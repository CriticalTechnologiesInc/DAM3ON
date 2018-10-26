package com.critical;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dom4j.DocumentException;
import org.pmw.tinylog.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import core.CTIConstants;
import core.TpmUtils;
import core.Utils;
import xacml.RequestMaker;
import xacml.SignatureHelper;
import xacml.XacmlParser;
import xacml.RequestMaker.Tuple;

/**
 * @author Justin Fleming - fleminjr@critical.com <br/>
 *         Jeremy Fields - fieldsjd@critical.com
 *
 */
@Path("/dam3on")
public class MultiLogin {

	/**
	 * 
	 * Description: This gets called from the web login interface for RADTIN. This handles making requests, sending request to be evaluated, and returning responses and advice
	 * to the login page.
	 * 
	 * 
	 * @param postData
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws DocumentException
	 */

	@POST
	@Path("/multi")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.TEXT_PLAIN)
	public Response doMulti(String postData, @Context HttpServletRequest requestContext) {
		core.TinyLogConfig.config();
		core.TinyLogConfig.logNewLine();
		Logger.info("Starting MultiLogin at {}", new Timestamp(new Date().getTime()));

		// {
		// "subject": "2geItSeVOKCA@critical.com", Only attest and none
		//
		// "tpm_quote": "...", Only for attest and both
		//
		// "pcrinfo": { Only for attest and both
		// "pcr17": "6D8E82BDBEED3E4A6C86193904E620453DE63DF9",
		// "indices": [17,
		// 19]
		// },
		//
		// "uuid": "d6f77765616a98de7419b6665e02b8a54aff35667084c1d8ad3d7b595a8dc8f3", Only for attest and both
		//
		// "files": [{ Only for attest and both
		// "bin-sha1": "9f4180525f416968f8f790e6998b8e33543d395f",
		// "order": 0
		// },
		// {
		// "cert-url": "http://google.com",
		// "bin-sha1": "17d5db4fe6d6a064191106824251002b17d0f6d0",
		// "order": 1
		// }],
		//
		// "action": "access", Only attest and none
		// "resource": "res_name" Only attest and none
		// "step": "s", All
		// "pgp_signature": "sig", Only auth and both
		// "signature": "sig", Only smart card auth
		// "certificate":"cert" Only smart card auth
		// }

		JsonObject jObj = new JsonParser().parse(postData).getAsJsonObject();
		String step = null;
		try {
			step = jObj.get("step").getAsString();
		} catch (Exception e) {
			Logger.error(e);
			return returnFail("No step");
		}
		if (step.equals("multi-one")) {
			return doMultiOne(step, jObj);
		} else if (step.equals("multi-attest-only")) {
			return doMultiAttestOnly(step, jObj, requestContext);
		} else if (step.equals("multi-auth") || step.equals("multi-both")) {
			return doMultiAuthBothPgp(step, jObj, requestContext);
		} else if (step.equals("multi-auth-sm") || step.equals("multi-both-sm")) {
			return doMultiAuthBothSmartCard(step, jObj, requestContext);
		} else {
			return returnFail("Bad step");
		}
	}

	private Response doMultiOne(String step, JsonObject jObj) {
		String action = null, resource = null;
		try {
			action = jObj.get("action").getAsString();
			resource = jObj.get("resource").getAsString();
		} catch (Exception e) {
			Logger.error(e);
			return returnFail("No resource or action");
		}

		Map<String, List<String>> params = httptools.util.getQueryParams(resource);
		if (params.keySet().size() > 0) {
			resource = resource.split("\\?")[0];
		}

		Logger.debug("Step: {}, Action {}, Resource {}", step, action, resource);

		String request = MultiUtil.formBasicSARRequest("anonymous", action, resource, null);
		if (request == null)
			return returnFail("Bad request (invalid resource)");
		String finalResult = MultiUtil.evaluateRequest(request);
		Logger.debug("Encoded Response: \n{}", finalResult);

		String decodedResponse = MultiUtil.decodeXACMLResponse(finalResult);
		Logger.debug("Decoded Response: \n{}", decodedResponse);

		SignatureHelper helper = new SignatureHelper();
		if (helper.verifyResponseSignature(decodedResponse)) { // don't need to check if empty, this returns false if it is
			Logger.info("SAML Signature was verified.");

			// String advice = MultiUtil.whatAdvice(decodedResponse);
			boolean attest = MultiUtil.getBoolAdvice(decodedResponse, "attest");
			boolean pgp = MultiUtil.getBoolAdvice(decodedResponse, "auth");
			boolean scard = MultiUtil.getBoolAdvice(decodedResponse, "scard");
			

			// Only minor difference between "attest" and "both" so they're combined
			if (attest || pgp || scard) {
				SARD sard = new SARD();
				sard.setAdviceBool("true");

				// Set Advice in response
				int len = 0;
				if (attest) {
					len += 1;
				}
				if (pgp) {
					len += 1;
				}
				if (scard) {
					len += 1;
				}
				String[] adv = new String[len];

				int counter = 0;
				if (attest) {
					adv[counter] = "attest";
					counter += 1;
				}

				if (pgp) {
					adv[counter] = "auth";
					counter += 1;
				}

				if (scard) {
					adv[counter] = "scard";
					counter += 1;
				}

				sard.setAdvice(adv);
				sard.setDecision(XacmlParser.getDecisionValueString(decodedResponse));
				sard.setCode("success");

				// If attestation is required, we must also add the PCR indicies required
				if (attest) {

					List<String> pcrIndicesStr = xacml.XacmlParser.getAdvice(decodedResponse, "pcr_index");
					boolean sable = MultiUtil.getBoolAdvice(decodedResponse, "sable_hash");
					
					if (sable){
						sard.setSableHash(true);
					}

					// convert pcr indices from strings to ints
					int[] pcrIndices = new int[pcrIndicesStr.size()];
					for (int i = 0; i < pcrIndicesStr.size(); i++) {
						pcrIndices[i] = Integer.parseInt(pcrIndicesStr.get(i));
					}

					Arrays.sort(pcrIndices);
					sard.setPcr_indices(pcrIndices);
				}

				return returnSuccess(new Gson().toJson(sard));
			} else {

				// Otherwise, we don't have any advice...
				String decisionValue = XacmlParser.getDecisionValueString(decodedResponse);
				SARD sard = new SARD();

				if (decisionValue.toLowerCase().equals("permit")) {
					String urlValue = XacmlParser.getAdvice(decodedResponse, "resource_url").get(0);
					sard.setUrl(urlValue);
					sard.setAdviceBool("false");
					if (urlValue == null)
						return returnFail("XACML Response did not contain a URL");
				}

				sard.setDecision(decisionValue);
				return returnSuccess(new Gson().toJson(sard));
			}
		} else {
			return returnFail("SAML signature was not verified.");
		}
	}

	private Response doMultiAttestOnly(String step, JsonObject jObj, HttpServletRequest requestContext) {
		// Attestation only request
		String action = null, resource = null;
		try {
			action = jObj.get("action").getAsString();
			resource = jObj.get("resource").getAsString();
		} catch (Exception e) {
			Logger.error(e);
			return returnFail("No resource or action");
		}

		Map<String, List<String>> params = httptools.util.getQueryParams(resource);
		String arg = null;
		if (params.keySet().size() > 0) {
			resource = resource.split("\\?")[0];
			try {
				arg = params.get("arg").get(0);
			} catch (NullPointerException npe) {
			}
		}

		jObj.remove("action");
//		jObj.remove("resource"); // needed for stupid QEMU work around to hard code for qemu demo resource
		jObj.remove("step");

		Logger.debug("JSON object to pass to HashCheck: {}", jObj.toString());
		String apiResponse = MultiUtil.checkHashApi(jObj.toString());
		if (apiResponse == null)
			return returnFail("Failed using HashCheck");

		Logger.debug("API Response from HashCheck: {}", apiResponse);
		JsonObject apiResponseObject = null;
		try{
			apiResponseObject = new JsonParser().parse(apiResponse).getAsJsonObject();
		}catch(Exception e){
			return returnFail("error parsing json");
		}

		String request = null;
		if (apiResponseObject.get("status").getAsString().equals("pass")) {
			RequestMaker r = new RequestMaker();
			List<Tuple<String, ?>> envs = getIpUaEnvs(requestContext);
			try {
				JsonArray pcrinfo_idx = jObj.get("pcrinfo").getAsJsonObject().get("indices").getAsJsonArray();
				int[] pcrinfo_idx_ia = new int[pcrinfo_idx.size()];

				// Extract numbers from JSON array.
				for (int i = 0; i < pcrinfo_idx.size(); ++i) {
					pcrinfo_idx_ia[i] = pcrinfo_idx.get(i).getAsInt();
				}

				for (int pcridx : pcrinfo_idx_ia) {
					if (pcridx != 19) {
						envs.add(r.new Tuple<String, String>("pcr" + pcridx, jObj.get("pcrinfo").getAsJsonObject().get("pcr" + pcridx).getAsString()));
					}
				}
				try{
					envs.add(r.new Tuple<String, String>("sable_hash", jObj.get("pcrinfo").getAsJsonObject().get("sable").getAsString()));
				}catch (NullPointerException npe){
					// sable wasn't requested, so ignore it
				}
			} catch (Exception e) {
				Logger.error(e);
				return returnFail("Missing a pcr hash");
			}

			Logger.info("HashCheck passed. Creating attest request now.");
			String subject = null;
			try {
				subject = jObj.get("subject").getAsString();
			} catch (Exception e) {
				Logger.error(e);
				return returnFail("No subject");
			}
			request = MultiUtil.formAttestRequest(subject, action, resource, envs, apiResponseObject);
		} else {
			return returnFail("Hash verification failed");
		}

		if (request == null) {
			return returnFail("Request returned null");
		}

		// Send XACML Request to PDP for evaluation
		String encoded_response = MultiUtil.evaluateRequest(request);
		Logger.debug("encoded_response: {}", encoded_response);

		// Decode PDP's XACML Response
		String decoded_response = MultiUtil.decodeXACMLResponse(encoded_response);
		Logger.debug("Decoded Response: \n{}", decoded_response);

		// Handling the Response...
		SignatureHelper helper = new SignatureHelper();
		if (helper.verifyResponseSignature(decoded_response)) { // don't need to check if empty, this returns false if it is
			String decisionValue = XacmlParser.getDecisionValueString(decoded_response);
			return insertPnonce(decoded_response, decisionValue, arg);
		} else {
			return returnFail("SAML signature was not verified.");
		}
	}

	private Response doMultiAuthBothSmartCard(String step, JsonObject jObj, HttpServletRequest requestContext) {
		String signature = null;
		String certificate = null;
		String action = null;
		String resource = null;

		try {
			signature = jObj.get("signature").getAsString();
			certificate = jObj.get("certificate").getAsString();
			action = jObj.get("action").getAsString();
			resource = jObj.get("resource").getAsString();
		} catch (Exception e) {
			Logger.error(e);
			return returnFail("No signature and/or certificate data");
		}

		Logger.debug("Signature: {}", signature);
		Logger.debug("Certificate: {}", certificate);
		X509Certificate x;
		try {
			x = Utils.loadNonExpiredCertificate(certificate);
			if (x == null){
				return returnFail("Certificate isn't CURRENT");
			}
		} catch (FileNotFoundException | CertificateException e1) {
			Logger.error(e1);
			return returnFail("Bad certificate");
		}
		String subject = Utils.extractEmail(x.getSubjectDN());

		Map<String, List<String>> params = httptools.util.getQueryParams(resource);
		String arg = null;
		if (params.keySet().size() > 0) {
			resource = resource.split("\\?")[0];
			try {
				arg = params.get("arg").get(0);
			} catch (NullPointerException npe) {
			}
		}
		String request = null;
		if (step.equals("multi-both-sm")) {

			jObj.addProperty("subject", subject);
			jObj.remove("action");
			jObj.remove("resource");
			jObj.remove("step");

			// Call the HashCheck API with previously created JSON
			Logger.debug("JSON object to pass to HashCheck: {}", jObj.toString());
			String apiResponse = MultiUtil.checkHashApi(jObj.toString());

			Logger.debug("API Response from HashCheck: {}", apiResponse);
			JsonObject apiResponseObject = new JsonParser().parse(apiResponse).getAsJsonObject();

			if (!(apiResponseObject.get("status").getAsString().equals("pass"))) {
				return returnFail("Hash verification failed");
			} else {
				RequestMaker r = new RequestMaker();
				List<Tuple<String, ?>> envs = getIpUaEnvs(requestContext);
				try {
					JsonArray pcrinfo_idx = jObj.get("pcrinfo").getAsJsonObject().get("indices").getAsJsonArray();
					int[] pcrinfo_idx_ia = new int[pcrinfo_idx.size()];

					// Extract numbers from JSON array.
					for (int i = 0; i < pcrinfo_idx.size(); ++i) {
						pcrinfo_idx_ia[i] = pcrinfo_idx.get(i).getAsInt();
					}

					for (int pcridx : pcrinfo_idx_ia) {
						if (pcridx != 19) {
							envs.add(r.new Tuple<String, String>("pcr" + pcridx, jObj.get("pcrinfo").getAsJsonObject().get("pcr" + pcridx).getAsString()));
						}
					}
					envs.add(r.new Tuple<String, String>("sable_hash", jObj.get("pcrinfo").getAsJsonObject().get("sable").getAsString()));
					envs.add(r.new Tuple<String, String>("signature", Utils.encodeBase64(signature)));
					envs.add(r.new Tuple<String, String>("certificate", Utils.encodeBase64(certificate)));
				} catch (Exception e) {
					Logger.error(e);
					return returnFail("Missing a pcr hash");
				}

				Logger.info("HashCheck passed. Creating attest request now.");
				request = MultiUtil.formAttestRequest(subject, action, resource, envs, apiResponseObject);
			}
		} else {
			RequestMaker r = new RequestMaker();
			List<Tuple<String, ?>> envs = getIpUaEnvs(requestContext);
			try {
				envs.add(r.new Tuple<String, String>("signature", Utils.encodeBase64(signature)));
				envs.add(r.new Tuple<String, String>("certificate", Utils.encodeBase64(certificate)));
			} catch (Exception e) {
				Logger.error(e);
			}
			request = MultiUtil.formBasicSARRequest(subject, action, resource, envs);
			if (request == null)
				return returnFail("Bad request (invalid resource)");
		}

		String encoded_response = MultiUtil.evaluateRequest(request);
		String decoded_response = MultiUtil.decodeXACMLResponse(encoded_response);
		Logger.debug("Response: \n" + decoded_response);
		SignatureHelper helper = new SignatureHelper();
		if (helper.verifyResponseSignature(decoded_response)) { // don't need to check if empty, this returns false if it is
			String decisionValue = XacmlParser.getDecisionValueString(decoded_response);
			return insertPnonce(decoded_response, decisionValue, arg);
		} else {
			return returnFail("SAML signature was not verified.");
		}

	}

	private Response doMultiAuthBothPgp(String step, JsonObject jObj, HttpServletRequest requestContext) {
		String pgpSignedData = null;
		try {
			pgpSignedData = jObj.get("pgp_signature").getAsString();
		} catch (Exception e) {
			Logger.error(e);
			return returnFail("No pgp signed data");
		}
		String[] arr = pgpSignedData.split("\n");
		String sub = null, act = null, res = null, nonce = null;

		try {
			for (int i = 0; i < arr.length; i++) {
				String temp = arr[i].toLowerCase();
				if (temp.contains("subject:"))
					sub = arr[i].substring(arr[i].indexOf(":") + 1);
				if (temp.contains("action:"))
					act = arr[i].substring(arr[i].indexOf(":") + 1);
				if (temp.contains("resource:"))
					res = arr[i].substring(arr[i].indexOf(":") + 1);
				if (temp.contains("nonce"))
					nonce = arr[i].substring(arr[i].indexOf(":") + 1);
			}
		} catch (Exception e) {
			return returnFail("Error processing PGP signature");
		}

		if (sub == null || act == null || res == null || nonce == null)
			return returnFail("PGP didn't have everything needed");

		Logger.debug("Subject Line: {}\nAction Line: {}\nResource Line: {}\nNonce Line: {}\n", sub, act, res, nonce);
		Logger.debug("Signature: {}", pgpSignedData);
		if (TpmUtils.getNonce(sub).equals(nonce)) {
			Map<String, List<String>> params = httptools.util.getQueryParams(res);
			String arg = null;
			if (params.keySet().size() > 0) {
				res = res.split("\\?")[0];
				try {
					arg = params.get("arg").get(0);
				} catch (NullPointerException npe) {
				}
			}
			String request = null;
			if (step.equals("multi-both")) {

				jObj.addProperty("subject", sub);
				jObj.remove("action");
				jObj.remove("resource");
				jObj.remove("step");

				// Call the HashCheck API with previously created JSON
				Logger.debug("JSON object to pass to HashCheck: {}", jObj.toString());
				String apiResponse = MultiUtil.checkHashApi(jObj.toString());

				Logger.debug("API Response from HashCheck: {}", apiResponse);
				JsonObject apiResponseObject = new JsonParser().parse(apiResponse).getAsJsonObject();

				if (!(apiResponseObject.get("status").getAsString().equals("pass"))) {
					return returnFail("Hash verification failed");
				} else {
					RequestMaker r = new RequestMaker();
					List<Tuple<String, ?>> envs = getIpUaEnvs(requestContext);
					try {
						JsonArray pcrinfo_idx = jObj.get("pcrinfo").getAsJsonObject().get("indices").getAsJsonArray();
						int[] pcrinfo_idx_ia = new int[pcrinfo_idx.size()];

						// Extract numbers from JSON array.
						for (int i = 0; i < pcrinfo_idx.size(); ++i) {
							pcrinfo_idx_ia[i] = pcrinfo_idx.get(i).getAsInt();
						}

						for (int pcridx : pcrinfo_idx_ia) {
							if (pcridx != 19) {
								envs.add(r.new Tuple<String, String>("pcr" + pcridx, jObj.get("pcrinfo").getAsJsonObject().get("pcr" + pcridx).getAsString()));
							}
						}
						envs.add(r.new Tuple<String, String>("sable_hash", jObj.get("pcrinfo").getAsJsonObject().get("sable").getAsString()));
						envs.add(r.new Tuple<String, String>("pgpsig", Utils.encodeBase64(pgpSignedData)));
					} catch (Exception e) {
						Logger.error(e);
						return returnFail("Missing a pcr hash");
					}

					Logger.info("HashCheck passed. Creating attest request now.");
					request = MultiUtil.formAttestRequest(sub, act, res, envs, apiResponseObject);
				}
			} else {
				RequestMaker r = new RequestMaker();
				List<Tuple<String, ?>> envs = getIpUaEnvs(requestContext);
				try {
					envs.add(r.new Tuple<String, String>("pgpsig", Utils.encodeBase64(pgpSignedData)));
				} catch (Exception e) {
					Logger.error(e);
				}
				request = MultiUtil.formBasicSARRequest(sub, act, res, envs);
				if (request == null)
					return returnFail("Bad request (invalid resource)");
			}

			String encoded_response = MultiUtil.evaluateRequest(request);
			String decoded_response = MultiUtil.decodeXACMLResponse(encoded_response);
			Logger.debug("Response: \n" + decoded_response);
			SignatureHelper helper = new SignatureHelper();
			if (helper.verifyResponseSignature(decoded_response)) { // don't need to check if empty, this returns false if it is
				String decisionValue = XacmlParser.getDecisionValueString(decoded_response);
				return insertPnonce(decoded_response, decisionValue, arg);
			} else {
				return returnFail("SAML signature was not verified.");
			}
		} else {
			return returnFail("Bad pub nonce match");
		}
	}

	private Response insertPnonce(String decoded_response, String decisionValue, String arg) {
		SARD sard = new SARD();
		if (decisionValue.toLowerCase().equals("permit")) { // this occasionally fails, even when it's permit in the response. idk why
			String decryptedCap;
			/** new ********/
			String fullPath = "";
			try {
				decryptedCap = MultiUtil.extractDecryptedCap(decoded_response);
				fullPath = XacmlParser.getSar(decoded_response, CTIConstants.RESOURCE_CATEGORY, "full-path");
			} catch (Exception e) {
				decryptedCap = null;
			}
			/***************/
			String subjectValue = XacmlParser.getSar(decoded_response, CTIConstants.SUBJECT_CATEGORY, CTIConstants.SUBJECT_ATTRIBUTEID);
			String actionValue = XacmlParser.getSar(decoded_response, CTIConstants.ACTION_CATEGORY, CTIConstants.ACTION_ATTRIBUTEID);
			String resourceValue = XacmlParser.getSar(decoded_response, CTIConstants.RESOURCE_CATEGORY, CTIConstants.RESOURCE_ATTRIBUTEID);
			String urlValue = XacmlParser.getAdvice(decoded_response, "resource_url").get(0);

			if (!fullPath.equals("")) {
				resourceValue = fullPath;
			}

			ArrayList<String> additionalUrls = XacmlParser.getAdvice(decoded_response, "additional_resource_url");
			String privateNonce = MultiUtil.generateNonce();

			Gson gson = new Gson();
			String jString = gson.toJson(additionalUrls);
			MultiUtil.removePnonce(subjectValue);

			if (urlValue == null) {
				return returnFail("XACML Response did not contain a URL");
			}

			String token = null;
			if (MultiUtil.accessTokenAdvicePresent(decoded_response)) {
				Logger.debug("Response DID containt Advice for a special token");
				token = MultiUtil.addTokenToDb(subjectValue, resourceValue);
				if (token != null)
					sard.setToken(token);
			}else{
				Logger.debug("Response did NOT contain Advice for a special token");
			}

			if (!MultiUtil.insertPrivateNonceDB(subjectValue, actionValue, resourceValue, decisionValue, urlValue, privateNonce, jString, decryptedCap, token, arg)) {
				return returnFail("There was a database error");
			}

			sard.setDecision(decisionValue);
			sard.setNonce(privateNonce);
			sard.setCode("success");
			return returnSuccess(new Gson().toJson(sard));
		} else {
			return returnFail("decision was " + decisionValue);
		}

	}

	private List<Tuple<String, ?>> getIpUaEnvs(HttpServletRequest requestContext) {
		RequestMaker r = new RequestMaker();
		List<Tuple<String, ?>> envs = new ArrayList<Tuple<String, ?>>();
		envs.add(r.new Tuple<String, String>("ip", requestContext.getRemoteAddr()));
		envs.add(r.new Tuple<String, String>("user_agent", requestContext.getHeader("User-Agent")));
		return envs;
	}

	/**
	 * 
	 * Description: Function for handling any errors or failures in the servlet. Returns @param reason in it's response to whatever called it.
	 * 
	 * @param reason
	 *            String to be logged that describes the error/reason it failed.
	 * @return HTML response
	 */
	private Response returnFail(String reason) {
		SARD fatErr = new SARD();
		Logger.error(reason);
		fatErr.setCode(reason);
		core.TinyLogConfig.killTinyLog();
		return Response.ok().entity(new Gson().toJson(fatErr)).header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "POST").allow("OPTIONS").build();
	}

	/**
	 * Description: Similar to returnFail, but for success. Takes in JSON to be returned, and returns it to HTTP requester.
	 * 
	 * @param json
	 *            to be included in response
	 * @return Response object
	 */
	private Response returnSuccess(String json) {
		core.TinyLogConfig.killTinyLog();
		return Response.ok().entity(json).header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "POST").allow("OPTIONS").build();
	}

	@SuppressWarnings("unused") // it's lying, it is used
	private class SARD {
		private String decision = "";
		private String code = "";
		private String nonce = "";
		private String url = "";
		private String adviceBool = "";
		private String token = "";
		private boolean sable_hash = false;
		private String[] advice;
		private int[] pcr_indices;

		public void setNonce(String nonce) {
			this.nonce = nonce;
		}

		public void setSableHash(boolean yn){
			this.sable_hash = yn;
		}
		
		public void setToken(String token) {
			this.token = token;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public void setPcr_indices(int[] pcr_indices) {
			this.pcr_indices = pcr_indices;
		}

		public void setDecision(String decision) {
			this.decision = decision;
		}

		public void setAdviceBool(String advicebool) {
			this.adviceBool = advicebool;
		}

		public void setAdvice(String[] adv) {
			this.advice = adv;
		}
	}

}

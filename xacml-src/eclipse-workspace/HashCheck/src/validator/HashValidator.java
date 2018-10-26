package validator;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pmw.tinylog.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import core.Utils;

/**
 * @author Justin Fleming - fleminjr@critical.com Jeremy Fields - fieldsjd@critical.com
 * 
 *         This servlet takes an HTTP post argument that is a JSON object containing details of files to be hashed and checked against the TPM quote
 *
 */
@WebServlet("/HashValidator")
public class HashValidator extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private String tpm_quote;
	private String uuid;
	private String subject;
	private String resource;
	private int[] pcrinfo_idx_ia;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		core.TinyLogConfig.config();
		Logger.info("In HashValidator @ {}", new Timestamp(new Date().getTime()));
		// Get JSON from the 'filesList' HTTP parameter
		String jString = null;
		try {
			jString = request.getParameter("filesList");
		} catch (Exception e) {
			returnFail(response, "filesList argument wasn't present");
			return;
		}

		// Convert value of parameter into Json object
		JsonArray jArr = null;
		JsonElement jElem = new JsonParser().parse(jString);
		JsonObject jObj = jElem.getAsJsonObject();
		try {
			jArr = jObj.get("files").getAsJsonArray();
			tpm_quote = jObj.get("tpm_quote").getAsString();
			uuid = jObj.get("uuid").getAsString();
			subject = jObj.get("subject").getAsString();
			resource = jObj.get("resource").getAsString();
			JsonArray pcrinfo_idx = jObj.get("pcrinfo").getAsJsonObject().get("indices").getAsJsonArray();

			pcrinfo_idx_ia = new int[pcrinfo_idx.size()];
			// Extract numbers from JSON array.
			for (int i = 0; i < pcrinfo_idx.size(); ++i) {
				pcrinfo_idx_ia[i] = pcrinfo_idx.get(i).getAsInt();
			}
		} catch (Exception e) {
			returnFail(response, "failed parsing json or finding 'files' element");
			return;
		}

		// Convert Json to ModFile objects for convenience
		ArrayList<ModFile> modFiles = HashValidatorUtils.convertJsonToPojoList(jArr);

		// Iterate over ModFile object and create a list of the binSha1
		// hashes
		Iterator<ModFile> iter = modFiles.iterator();
		//String[] hashes = new String[modFiles.size()*2];
		ArrayList<String> hashes = new ArrayList<String>();
		while (iter.hasNext()) {
			ModFile m = iter.next();
			if (m.hasOrder() && m.hasBinSha1()) {
				if (m.hasCertUrl()) { // in future when we can automate proof/cert/source validation
					Logger.debug("Binary DOES have a certificate URL with it");
					
					// Figure out whether this is a regular certificate, or an evidence certificate
					String type = HashValidatorUtils.whatTypeOfCertificate(Utils.getTextFromUrl(new URL(m.getCertificateUrl())));

					CTICertificate cert = null;
					// If it's a regular certificate ...
					if(type.equals("cert")){
						try {
							cert = new CTICertificate(new URL(m.getCertificateUrl()));
						} catch (Exception e) {
							Logger.error(e);
						}
						// the process it
						if (cert != null && cert.getSubjectHashInfoHash().equals(m.getBinSha1())) 
							HashValidatorUtils.processCertificateChain(cert, m.getBinSha1());
						
					}else if(type.equals("proof") || type.equals("pentest") || type.equals("insurance")){
						// otherwise deal with the evidence directly & non-recursively
						HashValidatorUtils.handleHighLevelEvidenceCert(cert, m.getBinSha1());
					}
					
				}
				
				if (core.Utils.isValidSHA1(m.getBinSha1()) ){
					hashes.add(m.getBinSha1());
					
					if (m.hasArg()){
						if(m.getOrder() != 0){ // 0 is sable arg. arg == binsha1, this avoids duplicate
							Logger.debug("Found a argument: " + m.getArg());
							String h1ah = core.Utils.byteArrayToHex(core.Utils.byteSha(m.getArg().getBytes()));
							Logger.debug("It's hash is: " + h1ah);
							hashes.add(h1ah);
						}
					}
				} else {
					Logger.error("Invalid SHA1 hash given: {}", m.getBinSha1());
					returnFail(response, "Invalid SHA1 hash given");
					return;
				}
			} else {
				Logger.error("ModFile object doesn't have enough details");
				returnFail(response, "Not enough details in ModFile");
				return;
			}
		}

		// All hashes check out, so now recreate and return the hash from
		// the TPM quote
		byte[] pcr19_hash;
		if(resource.equals("attestonly_qemu")){
			pcr19_hash = HashValidatorUtils.recalculatePCR19_qemu(hashes);
		}else{
			pcr19_hash = HashValidatorUtils.recalculatePCR19(hashes);
		}

		Logger.debug("Calculated hash: {}", core.Utils.byteArrayToHex(pcr19_hash));

		TreeMap<Integer, byte[]> valid = new TreeMap<Integer, byte[]>();
		for (int pcridx : pcrinfo_idx_ia) {
			if (pcridx == 19) {
				valid.put(19, pcr19_hash);
			} else {
				valid.put(pcridx, core.Utils.hexToBin(jObj.get("pcrinfo").getAsJsonObject().get("pcr" + pcridx).getAsString()));
			}
		}

		// Create tpm_getpcrhash 52 byte blob
		byte[] tpm_getpcrhash = HashValidatorUtils.tpmGetPcrHash(pcrinfo_idx_ia, valid, subject);
		String b64tpm_getpcrhash = core.Utils.encodeBase64(tpm_getpcrhash);
		Logger.debug("Base64 of calculated tpm_getpcrhash: {}", b64tpm_getpcrhash);
		Logger.debug("User provided b64 tpm_quote: {}", tpm_quote);
		Logger.debug("UUID of pubkey: {}", uuid);

		// Call command line tpm_verifyquote function and get a boolean result
		boolean result = HashValidatorUtils.tpmVerifyQuote(tpm_quote, uuid, b64tpm_getpcrhash);

		String result_s = result == true ? "pass" : "fail";
		Logger.info("Subject: {} quote verification result: {}", subject, result_s);

		JsonObject jo = new JsonObject();
		jo.addProperty("status", result_s);
		if (result) {
			// make hashes into JSON and return
			JsonArray j = new JsonArray();
			for (String item : hashes) {
				JsonElement je = new JsonPrimitive(item);
				j.add(je);
			}
			jo.add("hashList", j);
		} else {
			// make JSON with 'fail' and return
			jo.add("hashList", new JsonArray());
		}

		response.getWriter().write(jo.toString());
		core.TinyLogConfig.killTinyLog();
		return;
	}

	
	
	/**
	 * Description: General purpose function used to log an error/failure, tell client the reason, and execution should be stopped after this function is called.
	 * 
	 * @param response
	 *            Response object needed for writing something back to the caller
	 * @param reason
	 *            String describing why we're failing
	 */
	private void returnFail(HttpServletResponse response, String reason) {
		Logger.error(reason);
		try {
			response.getWriter().write("Error:" + reason);
		} catch (IOException e) {
			Logger.error(e);
		}
		core.TinyLogConfig.killTinyLog();
	}

}

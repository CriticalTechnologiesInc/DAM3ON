package points;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import core.CTIConstants;
import helpers.EXIDecoder;
import helpers.EXIEncoder;
import helpers.OpenSamlHelper;
import xacml.RequestMaker;
import xacml.SignatureHelper;
import xacml.RequestMaker.Tuple;

public class sample_pep {

	public static void main(String[] args) throws IOException {
		core.TinyLogConfig.config();

		// Create request
		RequestMaker r = new RequestMaker();
		List<Tuple<String, ?>> subs = new ArrayList<Tuple<String, ?>>();
		subs.add(r.new Tuple<String, String>(CTIConstants.SUBJECT_ATTRIBUTEID, "alice"));
		List<Tuple<String, ?>> acts = new ArrayList<Tuple<String, ?>>();
		acts.add(r.new Tuple<String, String>(CTIConstants.ACTION_ATTRIBUTEID, "access"));
		List<Tuple<String, ?>> ress = new ArrayList<Tuple<String, ?>>();
		ress.add(r.new Tuple<String, String>(CTIConstants.RESOURCE_ATTRIBUTEID, "pap"));
		String request = r.createRequest(subs, acts, ress, null);

		// Wrap request in SAML, and sign it
		OpenSamlHelper osh = new OpenSamlHelper();
		String signed_request = osh.wrapAndSignRequest(request);

		System.out.println("Request:\n" + signed_request + "\n");

		// EXI compress, and b64 encode it
		byte[] ba_request = new EXIEncoder().encodeEXIDefault(signed_request);
		String encoded_signed_request_b64 = Base64.getMimeEncoder().encodeToString(ba_request).replaceAll("\r", "").replaceAll("\n", "");

		// Utils.writeFile("c:\\users\\jeremy\\desktop\\enc_signed_req.txt", encoded_signed_request_b64);
		String json_response = evaluateRequest(encoded_signed_request_b64);
		JsonObject jObject = new JsonParser().parse(json_response).getAsJsonObject();
		String encoded_signed_response_b64 = jObject.get("SAMLResponse").toString();

		////// line between "create request" and "verify response" /////////

		// String encoded_signed_response_b64 = Utils.readFile("C:\\users\\jeremy\\desktop\\enc_signed_res.txt");
		byte[] exi_res = Base64.getMimeDecoder().decode(encoded_signed_response_b64);
		String signed_response = new EXIDecoder().decodeEXIDefault(exi_res);
		System.out.println("Response:\n"+signed_response+"\n");

		SignatureHelper helper = new SignatureHelper();

		if (helper.verifyResponseSignature(signed_response)) {
			String dec = xacml.XacmlParser.getDecisionValueString(signed_response);
			System.out.println("Signature WAS verified! - decision was: " + dec);
		} else {
			System.out.println("Signature was NOT verified!");
		}

		core.TinyLogConfig.killTinyLog();
	}

	/**
	 * Description: Calls PDP API to evaluate an XACML requests
	 * 
	 * @param request
	 *            XACML request as String
	 * @return response XACML response as String
	 */
	public static String evaluateRequest(String request) {
		HttpClient httpClient = HttpClientBuilder.create().build();

		HttpPost httpPost = new HttpPost("https://ctidev5.critical.com:8080/WebPDP/pdp");
		// Request parameters and other properties.
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("SAMLRequest", request));

		StringBuffer postResponse = null;
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, "US-ASCII"));
			HttpResponse response = httpClient.execute(httpPost);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			postResponse = new StringBuffer();
			String line2 = "";
			while ((line2 = rd.readLine()) != null) {
				postResponse.append(line2);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return postResponse.toString();
	}

}

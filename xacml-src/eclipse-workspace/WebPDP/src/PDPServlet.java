
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionQueryType;
import org.pmw.tinylog.Logger;
import org.wso2.balana.ctx.ResponseCtx;

import com.google.gson.Gson;

import xacml.SignatureHelper;
import pdp.PDP;
import pdp.PolicyDecisionPoint;
import helpers.EXIEncoder;
import helpers.OpenSamlHelper;
import helpers.EXIDecoder;

/**
 * This is a simple servlet that takes in a XACML request via POST,
 * runs it through the PDP, and then POSTs the response back to the
 * Tahoe PEP.
 * 
 * @author 
 * Justin Fleming - fleminjr@critical.com
 * Jeremy Fields - fieldsjd@critical.com
 *
 *
 */
@WebServlet("/pdp")
public class PDPServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		PrintWriter pw = null;
		try{
			pw = response.getWriter();
		}catch (IOException e){
			Logger.error(e);
			return;
		}
		
		pw.write("you found me");
		pw.close();
		return;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 * 
	 * This method will take in a XACML request from POST, evaluate, and then
	 * POST the XACML response back to the LoginWebInterfacePEP servlet.
	 * 
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		core.TinyLogConfig.config();
		Logger.info("Starting PDPServlet at {}", new Timestamp(new Date().getTime()));

		// get encoded XACMLAuthzDecisionQuery from POST request
		String encodedQuery = request.getParameter("SAMLRequest");
		Logger.debug("Encoded query: \n{}", encodedQuery);
		PrintWriter pw = null;
		try{
			pw = response.getWriter();
		}catch (IOException e){
			Logger.error(e);
			return;
		}
		
		try {
			byte[] binaryEXI = Base64.getMimeDecoder().decode(encodedQuery);
			EXIDecoder decoder = new EXIDecoder();
			SignatureHelper helper = new SignatureHelper();
			String decodedQuery = decoder.decodeEXIDefault(binaryEXI);
			
			XACMLAuthzDecisionQueryType xacmlAuthzDecisionQuery = (XACMLAuthzDecisionQueryType) helper.unmarshall(decodedQuery);
			Logger.debug("Decoded Query:\n\t{}",decodedQuery);
			
			String issuer = xacmlAuthzDecisionQuery.getIssuer().getValue();
			Logger.info("Issuer is {}",issuer);

			response.setContentType("application/json");
		    response.setCharacterEncoding("US-ASCII");

			Logger.info("Attempting to verify signature of request...");
			if (helper.verifyRequestSignature(decodedQuery)) {
				Logger.info("SAML was verified!\n");

				PDP pdp = new PDP(PolicyDecisionPoint.getPDPConfigWithDBBasedPolicyFinder()); 
			
				ResponseCtx rtx = pdp.evaluate(decodedQuery);
			
				OpenSamlHelper osh = new OpenSamlHelper();
				String samlResponse = osh.wrapAndSignRawResponse(rtx);
				if (samlResponse == null){
					pw.write("{\"status\":\"processing error\"}");
					core.TinyLogConfig.logNewLine();
					core.TinyLogConfig.killTinyLog();
					return;
				}
				
				Logger.debug("SAML response:\n{}",samlResponse);
				
				byte[] ba_response = new EXIEncoder().encodeEXIDefault(samlResponse);
				String encodedResponse = Base64.getMimeEncoder().encodeToString(ba_response).replace("\n", "").replace("\r","");

				Saml saml = new Saml();
				saml.setSAMLResponse(encodedResponse);
				String jsonString = new Gson().toJson(saml);
			    pw.write(jsonString);
				
			} else {
				Logger.warn("Signature was NOT verified.");
				pw.write("{\"status\":\"sig-not-verified\"}");
			}

		} catch (Exception e) {
			Logger.error(e);
			Saml saml = new Saml();
			saml.setSAMLResponse("Error Processing Request");
			String jsonString = new Gson().toJson(saml);
			pw.write(jsonString);
		}
		
		core.TinyLogConfig.logNewLine();
		core.TinyLogConfig.killTinyLog();
	}
	
	/**
	 * Dummy class needed for converting to JSON
	 */
	
	private class Saml {
		@SuppressWarnings("unused")
		private String SAMLResponse = "";
		public void setSAMLResponse(String sr){
			this.SAMLResponse = sr;
		}
	}
}
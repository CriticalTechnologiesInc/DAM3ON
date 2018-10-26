package points;

import java.io.IOException;
import java.util.Base64;

import org.wso2.balana.ctx.ResponseCtx;

import core.Utils;
import helpers.EXIDecoder;
import helpers.EXIEncoder;
import helpers.OpenSamlHelper;
import pdp.PDP;
import pdp.PolicyDecisionPoint;
import xacml.SignatureHelper;

public class sample_pdp {

	public static void main(String[] args) throws IOException {
		core.TinyLogConfig.config();
		
		String encoded_signed_request_b64 = Utils.readFile("C:\\users\\jeremy\\desktop\\q.txt");
		
//		PDP pdp = getPDPConfigWithFileBasedPolicyFinder("c:\\users\\jeremy\\desktop\\test\\");
		PDP pdp = new PDP(PolicyDecisionPoint.getPDPConfigWithDBBasedPolicyFinder()); 
		
		byte[] exi_req = Base64.getMimeDecoder().decode(encoded_signed_request_b64);
		String signed_request = new EXIDecoder().decodeEXIDefault(exi_req);
		
		SignatureHelper helper = new SignatureHelper();
		
		if(helper.verifyRequestSignature(signed_request)) {
			System.out.println("Signature WAS verified!");
			ResponseCtx rtx = pdp.evaluate(signed_request);
		
			OpenSamlHelper osh = new OpenSamlHelper();
			String samlResponse = osh.wrapAndSignRawResponse(rtx);
			
			System.out.println(samlResponse);
			
			byte[] ba_response = new EXIEncoder().encodeEXIDefault(samlResponse);
			String encoded_signed_response_b64 = Base64.getMimeEncoder().encodeToString(ba_response);
			
			Utils.writeFile("c:\\users\\jeremy\\desktop\\enc_signed_res.txt", encoded_signed_response_b64);
			
		}else {
			System.out.println("Signature was NOT verified!");
		}
		
		
		core.TinyLogConfig.killTinyLog();
	}

}

package helpers;

import java.io.FileNotFoundException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.pmw.tinylog.Logger;

import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.attr.BagAttribute;
import org.wso2.balana.attr.BooleanAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.cond.Evaluatable;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.cond.FunctionBase;

import core.Config;
import core.TpmUtils;
import core.Utils;

public class SmartCardAuth extends FunctionBase{
	// the names of the function, which will be used publicly
	public static final String NAME = "valid-smartcard";
	
    // the parameter types, in order, and whether or not they're bags
    private static final String params [] = {StringAttribute.identifier, StringAttribute.identifier};
    
    // This was a big problem. It thinks EnvironmentAttributeDesignator in policy is a bag.
    private static final boolean bagParams [] = {true, true};
    
    public SmartCardAuth() {
    	// Constructor indicating this function takes one string value as a parameter and returns a boolean ('false's indicate 'not bag')
    	// This is the constructor from line 191 of com.sun.xacml.cond.FunctionBase
    	// constructor args explained (in order):
    	// NAME = function name used in policy to invoke this function
    	// 0 = optional internal functionId (we don't use it so set it to 0)
    	// params = list of types of parameters passed into this function
    	// bagParams = list of booleans corresponding to whether or not params are a bag
    	// BooleanAttribute.identifier = fuction return type
    	// false = 'returns bag?'
    	super(NAME, 0, params , bagParams, BooleanAttribute.identifier, false);
    }
    
    @Override
    public EvaluationResult evaluate(List<Evaluatable> inputs, EvaluationCtx context) {
        // Evaluate the arguments
    	AttributeValue [] argValues = new AttributeValue[inputs.size()];
        EvaluationResult result = evalArgs(inputs, context, argValues);
        if (result != null){
            return result;
        }
        Logger.debug("In SmartCardAuth.java");
        
        BagAttribute certificate_bag = (BagAttribute)argValues[0];
        BagAttribute signature_bag = (BagAttribute)argValues[1];
                
        String certificate_s = null;
        Iterator<?> subIt = certificate_bag.iterator();
        while(subIt.hasNext()){
        	AttributeValue av = (AttributeValue) subIt.next();
        	certificate_s = Utils.decodeBase64(av.encode());
        }
        
        String sig = null;
        Iterator<?> actIt = signature_bag.iterator();
        while(actIt.hasNext()){
        	AttributeValue av = (AttributeValue) actIt.next();
        	sig = Utils.decodeBase64(av.encode());
        }
        
        Logger.debug("Checking signature against values: cert="+certificate_s+" sig="+sig);
        result = EvaluationResult.getFalseInstance();

		X509Certificate cert = null;
		try {
			cert = Utils.loadNonExpiredCertificate(certificate_s);
		} catch (FileNotFoundException | CertificateException e) {
			Logger.error(e);
		}
		
		if (cert == null)
			return EvaluationResult.getFalseInstance(); 
		
		String ca_cert = Utils.readFile(Config.Certificates.ca_path);
		
		Date cur = new Date();
		if (cur.after(cert.getNotAfter())){
			Logger.error("Cert is being used after it expired");
			return EvaluationResult.getFalseInstance();
		}
		
		if (cur.before(cert.getNotBefore())){
			Logger.error("Cert is being used before it is valid");
			return EvaluationResult.getFalseInstance();
		}
		
		
        boolean sig_res = validateSignature(cert, sig);
        boolean ca_res = validateAgainstCA(cert, ca_cert);
        
        Logger.debug("Signature verification result: {}", sig_res);
        Logger.debug("CA verification result: {}", ca_res);
        
        if(sig_res && ca_res)
        	result = EvaluationResult.getTrueInstance();
        
        return result;
    }
    
	
	private static boolean validateSignature(X509Certificate cert, String sig_h) {
		try {

			Signature sig = null;

			sig = Signature.getInstance("SHA256withRSA");
			sig.initVerify(cert);
			String email = Utils.extractEmail(cert.getSubjectDN());
			String data_s = TpmUtils.getNonce(email);
			sig.update(data_s.getBytes());

			byte[] provided_sig = Utils.hexToBin(sig_h);
			return sig.verify(provided_sig);
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
	
	private static boolean validateAgainstCA(X509Certificate cert, String ca_cert) {
		try {
			Certificate ca = Utils.loadNonExpiredCertificate(ca_cert);
			
			if (ca == null){
				Logger.error("Error loading CA certificate - or CA is not CURRENT");
				return false;
			}
		
			PublicKey capk = ca.getPublicKey();
			cert.verify(capk);
			return true;
		} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException | FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
	

}

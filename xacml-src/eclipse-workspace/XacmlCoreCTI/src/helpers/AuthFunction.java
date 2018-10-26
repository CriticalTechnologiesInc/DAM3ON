package helpers;

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
import core.Utils;
import core.GPGHelperCLI;
import core.TpmUtils;

public class AuthFunction extends FunctionBase{
	// the names of the function, which will be used publicly
	public static final String NAME = "valid-pgp";

    // the parameter types, in order, and whether or not they're bags
    private static final String params [] = {StringAttribute.identifier, StringAttribute.identifier, StringAttribute.identifier, StringAttribute.identifier};
    
    // This was a big problem. It thinks EnvironmentAttributeDesignator in policy is a bag.
    private static final boolean bagParams [] = {true, true, true, true};
    
    public AuthFunction() {
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
        Logger.debug("In AuthFunction.java");
        
        BagAttribute subject = (BagAttribute)argValues[0];
        BagAttribute action = (BagAttribute)argValues[1];
        BagAttribute resource = (BagAttribute)argValues[2];
        BagAttribute signature = (BagAttribute)argValues[3];
                
        String email = null;
        Iterator<?> subIt = subject.iterator();
        while(subIt.hasNext()){
        	AttributeValue av = (AttributeValue) subIt.next();
        	email = av.encode();
        }
        
        String expected_action = null;
        Iterator<?> actIt = action.iterator();
        while(actIt.hasNext()){
        	AttributeValue av = (AttributeValue) actIt.next();
        	expected_action = av.encode();
        }
        
        String expected_resource = null;
        Iterator<?> resIt = resource.iterator();
        while(resIt.hasNext()){
        	AttributeValue av = (AttributeValue) resIt.next();
        	expected_resource = av.encode();
        }
        
        Logger.debug("Checking signature against values: res="+expected_resource+" act="+expected_action+" sub="+email);
        
        String sig = null;
        Iterator<?> sigIt = signature.iterator();
        while(sigIt.hasNext()){
        	AttributeValue av = (AttributeValue) sigIt.next();
        	sig = av.encode();
        }
        String decodedSig;
        try{
        	decodedSig = Utils.decodeBase64(sig);
        }catch (NullPointerException e){
        	return EvaluationResult.getFalseInstance();
        }

        Logger.debug("Email: {}\nSig:{}", email, decodedSig);
        String[] arr = decodedSig.split("\n");
		String given_nonce = null;
		String given_action = null;
		String given_resource = null;

		try{
			for (int i = 0; i < arr.length; i++) {
				String temp = arr[i].toLowerCase();
				if (temp.contains("nonce:"))
					given_nonce = arr[i].substring(arr[i].indexOf(":") + 1);
				if (temp.contains("resource:"))
					given_resource = arr[i].substring(arr[i].indexOf(":") + 1);
				if (temp.contains("action:"))
					given_action = arr[i].substring(arr[i].indexOf(":") + 1);
			}
		}catch(Exception e){
			Logger.error("Failed to get nonce from signature:\n{}",e);
		}
        
        Logger.debug("Given nonce:{}",given_nonce);
        Logger.debug("from signature; resource: {} action: {}",given_resource,given_action);
        
        if(given_resource.contains("?")){ // incase resource contains an argument
        	given_resource = given_resource.split("\\?")[0];
        }
        
        if(resource_is_file(given_resource)){
			// copy argument 'resource' to 'resource_s' so that we can keep existing code still using 'resource'
        	String resource_s = given_resource;
        	
			// Strip string of 'file://' and split on '/' to get length of path to file
			String[] split = resource_s.substring(7).split("/");
			
			// Repo name is 0th element of split
			given_resource = split[0];
        }
        
        String expected_nonce = TpmUtils.getNonce(email);
        if(expected_nonce.equals(given_nonce) && expected_action.equals(given_action) && expected_resource.equals(given_resource)){ // TODO .toLower() would be safer on action & resource
			try {
				Logger.debug("Email: {}\nSig:{}", email, sig);
				if(GPGHelperCLI.verifySignature(email, decodedSig)){
					result = EvaluationResult.getTrueInstance();
				}else {
					Logger.debug("The signature did not verify w/ GPG (or possibly couldn't find the public key");
					result = EvaluationResult.getFalseInstance();
				}
			} catch (Exception ioe) {
				Logger.error(ioe);
				result = EvaluationResult.getFalseInstance();
			}
        } else {
        	Logger.debug("One of the signed values did not match the expected values");
        	Logger.debug("nonce     ~ given: {} -- expected: {}",given_nonce, expected_nonce);
        	Logger.debug("action    ~ given: {} -- expected: {}",given_action, expected_action);
        	Logger.debug("resource  ~ given: {} -- expected: {}",given_resource, expected_resource);
        	result = EvaluationResult.getFalseInstance();
        }
        TpmUtils.deletePubNonce(email);
        
        if(result == EvaluationResult.getTrueInstance())
        	Logger.debug("PGP signature verified");
        else
        	Logger.debug("PGP signature NOT verified");
        
        return result;
        
    }

	private static boolean resource_is_file(String res) {
		try {
			if (res.substring(0, 7).toLowerCase().equals("file://")) {
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
    
}

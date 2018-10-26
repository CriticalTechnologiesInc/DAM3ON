package helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import iptools.IpRangeCheck;

import org.pmw.tinylog.Logger;

import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.attr.BagAttribute;
import org.wso2.balana.attr.BooleanAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.cond.Evaluatable;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.cond.FunctionBase;

/**
 * This is what a valid corresponding condition looks like
 * <Condition FunctionId="ip-on-whitelist">
			<Apply FunctionId="urn:oasis:names:tc:xacml:1.0:function:string-bag">
				<AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">1.1.1.1/28</AttributeValue>
				<AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">2.2.2.2/24</AttributeValue>
			</Apply>
			<EnvironmentAttributeDesignator AttributeId="ip" DataType="http://www.w3.org/2001/XMLSchema#string"/>
		</Condition>
 * 
 */



public class IpListFunction extends FunctionBase{
	// the name of the function, which will be used publicly
    public static final String NAME_ON_WHITELIST = "ip-on-whitelist";
    // If you wanted to use the standard name space you'd do:
//    public static final String NAME_ON_WHITELIST = FunctionBase.FUNCTION_NS + "ip-on-whitelist";
    
    public static final String NAME_ON_BLACKLIST = "ip-not-on-blacklist";

    // the parameter types, in order, and whether or not they're bags
    private static final String params [] = {StringAttribute.identifier, StringAttribute.identifier};
    
    // This was a big problem. It thinks EnvironmentAttributeDesignator in policy is a bag.
    private static final boolean bagParams [] = {true, true};
    
    private static final int ID_WHITELIST = 0;
    private static final int ID_BLACKLIST = 1;
    
    /**
     * Returns a <code>Set</code> containing all the function identifiers
     * supported by this class.
     *
     * @return a <code>Set</code> of <code>String</code>s
     */
    public static Set<Integer> getSupportedIdentifiers() {
        Set<Integer> set = new HashSet<Integer>();

        set.add(ID_WHITELIST);
        set.add(ID_BLACKLIST);

        return set;
    }
    
    /**
     * Private helper that returns the internal identifier used for the
     * given standard function.
     */
    private static int getId(String functionName) {
        if (functionName.equals(NAME_ON_WHITELIST))
            return ID_WHITELIST;
        else if (functionName.equals(NAME_ON_BLACKLIST))
            return ID_BLACKLIST;
        else
            throw new IllegalArgumentException("unknown add function " +
                                               functionName);
    }
    
    
    public IpListFunction(String functionName) {
    	// Constructor indicating this function takes one string value as a parameter and returns a boolean ('false's indicate 'not bag')
    	// This is the constructor from line 191 of com.sun.xacml.cond.FunctionBase
    	// constructor args explained (in order):
    	// NAME = function name used in policy to invoke this function
    	// 0 = optional internal functionId (we don't use it so set it to 0)
    	// params = list of types of parameters passed into this function
    	// bagParams = list of booleans corresponding to whether or not params are a bag
    	// BooleanAttribute.identifier = fuction return type
    	// false = 'returns bag?'
    	super(functionName, getId(functionName), params , bagParams, BooleanAttribute.identifier, false);
    }
    
    @Override
    public EvaluationResult evaluate(List<Evaluatable> inputs, EvaluationCtx context) {
    	String usersIp = null;
    	Logger.debug("In IpListFunction.java");
        // Evaluate the arguments
    	AttributeValue [] argValues = new AttributeValue[inputs.size()];
        EvaluationResult result = evalArgs(inputs, context, argValues);
        if (result != null){
            return result;
        }
        
        BagAttribute ipList = (BagAttribute)argValues[0];
        BagAttribute userIp = (BagAttribute)argValues[1];
        
        ArrayList<String> whitelist = new ArrayList<String>();
        Iterator<?> ipList_it = ipList.iterator();
        while(ipList_it.hasNext()){
        	AttributeValue av = (AttributeValue) ipList_it.next();
        	whitelist.add(av.encode());
        }
        
        Iterator<?> userIp_it = userIp.iterator();
        while(userIp_it.hasNext()){
        	AttributeValue av = (AttributeValue) userIp_it.next();
        	usersIp = av.encode();
        }

        boolean inRange = false;
        try {
			inRange = IpRangeCheck.isInRange(usersIp, whitelist);
		} catch (Exception e) {
			return EvaluationResult.getFalseInstance();
		}
        
        String dbg = getFunctionId() == 0 ? "whitelist" : "blacklist";    
        if(inRange)
        	Logger.debug("User " + usersIp + " IS on the "  + dbg);
        else
        	Logger.debug("User " + usersIp + " is NOT on the "  + dbg);
        
        switch (getFunctionId()) {
	        case ID_WHITELIST:{
	        	if(inRange){
	    			result = EvaluationResult.getTrueInstance();
	    			break;
	        	}else{
	    			result = EvaluationResult.getFalseInstance();
	    			break;
	        	}
	        }
	        case ID_BLACKLIST:{
	        	if(inRange){
	        		result = EvaluationResult.getFalseInstance();
	        		break;
	        	}else{
	    			result = EvaluationResult.getTrueInstance();
	    			break;
	        	}
	        }
	        default:{
	        	result = EvaluationResult.getFalseInstance();
	        }
        }
        
        
        return result;
		
        
    }
}

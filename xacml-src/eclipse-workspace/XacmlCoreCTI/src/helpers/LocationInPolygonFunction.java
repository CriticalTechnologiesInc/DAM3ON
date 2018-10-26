package helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.pmw.tinylog.Logger;

import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.attr.BagAttribute;
import org.wso2.balana.attr.BooleanAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.cond.Evaluatable;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.cond.FunctionBase;


public class LocationInPolygonFunction extends FunctionBase{
	// the names of the function, which will be used publicly
	public static final String NAME_LAT_LONG_WHITELIST = "lat-long-in-polygon";
	public static final String NAME_LAT_LONG_BLACKLIST = "lat-long-not-in-polygon";

	
	// Internal identifiers for each supported function
	private static final int ID_LAT_LONG_WHITELIST = 0;
	private static final int ID_LAT_LONG_BLACKLIST = 1;

    // the parameter types, in order, and whether or not they're bags
    private static final String params [] = {StringAttribute.identifier, StringAttribute.identifier};
    
    // This was a big problem. It thinks EnvironmentAttributeDesignator in policy is a bag.
    private static final boolean bagParams [] = {true, true};

    /**
     * Private helper that returns the internal identifier used for the
     * given standard function.
     */
    private static int getId(String functionName) {
        if (functionName.equals(NAME_LAT_LONG_WHITELIST))
            return ID_LAT_LONG_WHITELIST;
        else if (functionName.equals(NAME_LAT_LONG_BLACKLIST))
            return ID_LAT_LONG_BLACKLIST;
        else
            throw new IllegalArgumentException("unknown add function " +
                                               functionName);
    }
    
    /**
     * Returns a <code>Set</code> containing all the function identifiers
     * supported by this class.
     *
     * @return a <code>Set</code> of <code>String</code>s
     */
    public static Set<Integer> getSupportedIdentifiers() {
        Set<Integer> set = new HashSet<Integer>();

        set.add(ID_LAT_LONG_WHITELIST);
        set.add(ID_LAT_LONG_BLACKLIST);

        return set;
    }
    
    public LocationInPolygonFunction(String functionName) {
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
        // Evaluate the arguments
    	AttributeValue [] argValues = new AttributeValue[inputs.size()];
        EvaluationResult result = evalArgs(inputs, context, argValues);
        if (result != null){
            return result;
        }
        Logger.debug("In LocationInPolygonFunction.java");
        
        BagAttribute matchList = (BagAttribute)argValues[0];
        BagAttribute latlong = (BagAttribute)argValues[1];
                
        ArrayList<String> matchVals = new ArrayList<String>();
        Iterator<?> matchList_it = matchList.iterator();
        while(matchList_it.hasNext()){
        	AttributeValue av = (AttributeValue)matchList_it.next();
        	matchVals.add(av.encode().toLowerCase()); // lower everything to be safer
        }
        
        String latlong_s = null;
        Iterator<?> latlong_it = latlong.iterator();
        while(latlong_it.hasNext()){
        	AttributeValue av = (AttributeValue)latlong_it.next();
        	latlong_s = av.encode();
        }

        debugLog(matchVals, latlong_s);
        result = EvaluationResult.getFalseInstance();

        double lat = Double.parseDouble(latlong_s.split(",")[0]);
        double lon = Double.parseDouble(latlong_s.split(",")[1]);
        
    	boolean geolocation_result;
    	try {
    		geolocation_result = geotools.GeoPolygonCheck.IsLatLongInsidePolygon(matchVals, lat, lon);
		} catch (Exception e) {
			return EvaluationResult.getFalseInstance(); // invalid coordinates
		}
    	if(geolocation_result)
    		result = EvaluationResult.getTrueInstance();


        if(result == EvaluationResult.getTrueInstance())
        	Logger.debug("User WAS in the polygon");
        else
        	Logger.debug("User was NOT in the polygon");
        
        switch(getFunctionId()){
        case ID_LAT_LONG_WHITELIST:
        	return result;
        case ID_LAT_LONG_BLACKLIST:{
        	if(result == EvaluationResult.getTrueInstance()) // if user WAS in polygon blacklist
        		return EvaluationResult.getFalseInstance(); // then fail
        	else
        		return EvaluationResult.getTrueInstance(); // otherwise, they weren't in it, so succeed
        }
        default:
        	return EvaluationResult.getFalseInstance(); // if something goes wrong, fail
        }
        
    }
    
    private void debugLog(ArrayList<String> matchvals, String ip){
    	String tmp = "";
    	
    	for(String s : matchvals)
    		tmp += s + ",";

		switch(getFunctionId()){
		case ID_LAT_LONG_WHITELIST:{
			Logger.debug("Doing Lat/Long polygon check on user: " + ip + " against values: " + tmp + " which is a whitelist");
			break;
		}
		case ID_LAT_LONG_BLACKLIST:{
			Logger.debug("Doing Lat/Long polygon check on user: " + ip + " against values: " + tmp + " which is a blacklist");
			break;
		}
		}
    }
}

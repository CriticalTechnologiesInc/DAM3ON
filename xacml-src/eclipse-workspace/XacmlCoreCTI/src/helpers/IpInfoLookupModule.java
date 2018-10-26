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
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.finder.AttributeFinderModule;

import iptools.IpLookUp;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Extension of Sun's AttributeFinderModule. This module takes in an ip address from the XACML Request and looksup various info (based on the AttributeId). 
 * @author 
 * Justin Fleming - fleminjr@critical.com
 * Jeremy Fields - fieldsjd@critical.com
 *
 */
public class IpInfoLookupModule extends AttributeFinderModule {

    private URI defaultId;

    public IpInfoLookupModule() {

        try {
        	defaultId = new URI("ip");
        } catch (URISyntaxException e) {
           //ignore
        }

    }

    @Override
    public Set<String> getSupportedCategories() {
        Set<String> categories = new HashSet<String>();
        categories.add("urn:oasis:names:tc:xacml:3.0:attribute-category:environment");
        return categories;
    }

    @Override
    public Set<String> getSupportedIds() {
        Set<String> ids = new HashSet<String>();
        ids.add("ip-city");
        ids.add("ip-lat-long");
        ids.add("ip-country");
        ids.add("ip-region");
        ids.add("ip-timezone");
        ids.add("ip-zipcode");
        return ids;   
    }
    
    @Override
    public boolean isDesignatorSupported() {
        return true;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sun.xacml.finder.AttributeFinderModule#findAttribute(java.net.URI,
	 * java.net.URI, java.net.URI, java.net.URI, com.sun.xacml.EvaluationCtx,
	 * int)
	 */
    @Override
	public EvaluationResult findAttribute(URI attributeType, URI attributeId, String issuer, URI envCategory,
			EvaluationCtx context) {

		Logger.debug("In IpInfoLookupModule for: " + attributeId.toString());
		
		ArrayList<String> res = new ArrayList<String>(); 
		try {
			// Get host ip from XACML request
			EvaluationResult hostIp = context.getAttribute(new URI(StringAttribute.identifier), defaultId, issuer, envCategory);
			String ipString = null;
			if (hostIp.getAttributeValue().isBag()) {
				BagAttribute attr = (BagAttribute) hostIp.getAttributeValue();
				for (Iterator<?> iterator = attr.iterator(); iterator.hasNext();) {
					ipString = ((AttributeValue) iterator.next()).encode();
					break;
				}
			}
			
			IpLookUp ilu = new IpLookUp(ipString);
			
			if (attributeId.toString().equals("ip-city")) {
				res.add(ilu.getCity());
			} else if (attributeId.toString().equals("ip-country")) {
				res.add(ilu.getCountry_code());
				res.add(ilu.getCountry_name());
			} else if (attributeId.toString().equals("ip-region")) {
				res.add(ilu.getRegion_code());
				res.add(ilu.getRegion_name());
			} else if (attributeId.toString().equals("ip-timezone")) {
				res.add(ilu.getTime_zone());
			} else if (attributeId.toString().equals("ip-zipcode")) {
				res.add(ilu.getZip_code());
			}  else if (attributeId.toString().equals("ip-lat-long")) {
				res.add(ilu.getLatitude()+","+ilu.getLongitude());
			} 
		} catch (Exception e) {
			Logger.error(e);
		}

		List<AttributeValue> attributeValues = new ArrayList<AttributeValue>();
		for(String result: res){
			// Make sure to give policy authors a warning that designators using these id's will return all lower string values
			// thus their policies should match this all lower case convention.
			Logger.debug("Adding: {} to the result bag for function: {}", result, attributeId.toString());
			attributeValues.add(new StringAttribute(result.toLowerCase()));
		}

		return new EvaluationResult(new BagAttribute(attributeType, attributeValues));
	}
}
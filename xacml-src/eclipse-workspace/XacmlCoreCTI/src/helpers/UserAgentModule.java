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

import httptools.UserAgentParser;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Extension of Sun's AttributeFinderModule. This module queries a database using the subject-id of the XACML
 *  Request to retrieve associated value and verifies that it matches the asserted value. 
 * @author 
 * Justin Fleming - fleminjr@critical.com
 * Jeremy Fields - fieldsjd@critical.com
 *
 */
public class UserAgentModule extends AttributeFinderModule {

    private URI defaultSubjectId;

    public UserAgentModule() {

        try {
            defaultSubjectId = new URI("user-agent");
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
        ids.add("ua-browser-type");
        ids.add("ua-browser-manufacturer");
        ids.add("ua-browser-name");
        ids.add("ua-browser-render-engine");
        ids.add("ua-browser-version");
        ids.add("ua-os-manufacturer");
        ids.add("ua-os-name");
        ids.add("ua-device-type");
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
		Logger.info("In UserAgentModule...");

		EvaluationResult user_agent_er = null;
		String user_agent = null;
		
		try {
			// TODO - if this module stops working change the issuer param of this call back to environmentCategory
			user_agent_er = context.getAttribute(new URI(StringAttribute.identifier),
					defaultSubjectId, issuer, envCategory);
			if (user_agent_er.getAttributeValue().isBag()) {
				BagAttribute attr = (BagAttribute) user_agent_er.getAttributeValue();
				for (Iterator<?> iterator = attr.iterator(); iterator.hasNext();) {
					AttributeValue val = (AttributeValue) iterator.next();
					user_agent = val.encode();
					break;
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}

		UserAgentParser uap = new UserAgentParser(user_agent);
		String result = null;
		
		if(attributeId.toString().equals("ua-browser-type")){
			result = uap.getBrowserTypeName();
		}else if(attributeId.toString().equals("ua-browser-manufacturer")){
			result = uap.getBrowserManufacturerName();
		}else if(attributeId.toString().equals("ua-browser-name")){
			result = uap.getBrowserName();
		}else if(attributeId.toString().equals("ua-browser-render-engine")){
			result = uap.getBrowserRenderEngineName();
		}else if(attributeId.toString().equals("ua-browser-version")){
			result = uap.getBrowserVersion();
		}else if(attributeId.toString().equals("ua-os-manufacturer")){
			result = uap.getOsMan();
		}else if(attributeId.toString().equals("ua-os-name")){
			result = uap.getOsName();
		}else if(attributeId.toString().equals("ua-device-type")){
			result = uap.getDeviceType();
		}
		
		Logger.debug("Getting: " + attributeId.toString() + " which is: " + result);
		
		List<AttributeValue> attributeValues = new ArrayList<AttributeValue>();
		attributeValues.add(new StringAttribute(result));

		return new EvaluationResult(new BagAttribute(attributeType, attributeValues));
	}
}
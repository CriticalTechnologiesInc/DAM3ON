package helpers;

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

import xacml.Util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * 
 * Custom PDP module used for verifying roles of an 'Onbehalf' request, by retrieving values
 * from a database.
 * Example; You can only "subscribe" someone else, if you are a [Manager|CEO] and the user is an 'Employee'
 *
 * @author 
 * Justin Fleming - fleminjr@critical.com
 * Jeremy Fields - fieldsjd@critical.com
 *
 */
public class OnBehalfOfRoleModule extends AttributeFinderModule {


    private URI defaultSubjectId;

    public OnBehalfOfRoleModule() {

        try {
            defaultSubjectId = new URI("subject-on-behalf-of-id");
        } catch (URISyntaxException e) {
           //ignore
        }

    }

    @Override
    public Set<String> getSupportedCategories() {
        Set<String> categories = new HashSet<String>();
        categories.add("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject");
        return categories;
    }

	// always return true, since this is a feature we always support
	/* (non-Javadoc)
	 * @see com.sun.xacml.finder.AttributeFinderModule#isDesignatorSupported()
	 */
    @Override
	public boolean isDesignatorSupported() {
		return true;
	}

    @Override
    public Set<String> getSupportedIds() {
        Set<String> ids = new HashSet<String>();
        ids.add("subject-on-behalf-of-role");
        return ids;   
    }
	/* (non-Javadoc)
	 * @see com.sun.xacml.finder.AttributeFinderModule#findAttribute(java.net.URI, java.net.URI, java.net.URI, java.net.URI, com.sun.xacml.EvaluationCtx, int)
	 */
    @Override
	public EvaluationResult findAttribute(URI attributeType, URI attributeId, String issuer, URI subjectCategory,
			EvaluationCtx context) {


		EvaluationResult subject = null;
		String subjectId = null;
		//System.out.println("Trying to find the subject id now...");
		try {
			subject = context.getAttribute(new URI(StringAttribute.identifier),
					defaultSubjectId, issuer, subjectCategory);
			if (subject.getAttributeValue().isBag()) {
				BagAttribute attr = (BagAttribute) subject.getAttributeValue();
				for (Iterator<?> iterator = attr.iterator(); iterator.hasNext();) {
					AttributeValue val = (AttributeValue) iterator.next();
					subjectId = val.encode();
					break;
				}
			}
			//System.out.println("The subject on behalf of id is: " + subjectId);
		} catch (Exception e) {
			Logger.error(e);
		}
	
		
		ArrayList<String> rs = null;
		try {
			rs = Util.readRoles(subjectId);
		} catch (Exception e) {
			Logger.error(e);
			rs = new ArrayList<String>();
		}
		List<AttributeValue> attributeValues = new ArrayList<AttributeValue>();
		for (int i = 0; i < rs.size(); i++) {
			attributeValues.add(new StringAttribute(rs.get(i)));
			//System.out.println("Just added " + rs.get(i) + " to string bag");
		}
		return new EvaluationResult(new BagAttribute(attributeType, attributeValues));
	}

}

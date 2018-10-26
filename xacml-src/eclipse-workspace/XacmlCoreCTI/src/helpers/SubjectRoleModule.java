package helpers;

import java.util.HashSet;
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
 * Extension of Sun's AttributeFinderModule. This module queries a database using the subject-id of the XACML
 *  Request to retrieve associated roles.
 * @author 
 * Justin Fleming - fleminjr@critical.com
 * Jeremy Fields - fieldsjd@critical.com
 *
 */
public class SubjectRoleModule extends AttributeFinderModule {

    private URI defaultSubjectId;

    public SubjectRoleModule() {

        try {
            defaultSubjectId = new URI("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
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

    @Override
    public Set<String> getSupportedIds() {
        Set<String> ids = new HashSet<String>();
        ids.add("role");
        return ids;   
    }
    
    @Override
    public boolean isDesignatorSupported() {
        return true;
    }
    
    @Override
    public EvaluationResult findAttribute(URI attributeType, URI attributeId, String issuer,
                                                            URI category, EvaluationCtx context) {
        String subjectId = null;
        EvaluationResult result = context.getAttribute(attributeType, defaultSubjectId, issuer, category);
        if(result != null && result.getAttributeValue() != null && result.getAttributeValue().isBag()){
            BagAttribute bagAttribute = (BagAttribute) result.getAttributeValue();
            if(bagAttribute.size() > 0){
                subjectId = ((AttributeValue) bagAttribute.iterator().next()).encode();

            }
        }
		ArrayList<String> rs = null;
		try {
			rs = Util.readRoles(subjectId);
		} catch (Exception e) {
			Logger.error(e);
		}

		List<AttributeValue> attributeValues = new ArrayList<AttributeValue>();
		for (int i = 0; i < rs.size(); i++) {
			attributeValues.add(new StringAttribute(rs.get(i)));
			Logger.debug("Just added {} to string bag", rs.get(i));
		}
		
		return new EvaluationResult(new BagAttribute(attributeType, attributeValues));
	}

}

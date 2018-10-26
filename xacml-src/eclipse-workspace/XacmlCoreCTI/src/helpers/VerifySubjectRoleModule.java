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
 * Extension of Sun's AttributeFinderModule. This module queries a database using the subject-id of the XACML
 *  Request to retrieve associated roles and only returns if the asserted role matches the result of the database query.
 * @author 
 * Justin Fleming - fleminjr@critical.com
 * Jeremy Fields - fieldsjd@critical.com
 *
 */
public class VerifySubjectRoleModule extends AttributeFinderModule {
	private URI defaultSubjectId;
	
    public VerifySubjectRoleModule() {

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
	public boolean isDesignatorSupported() {
		return true;
	}
    
    @Override
    public Set<String> getSupportedIds() {
        Set<String> ids = new HashSet<String>();
        ids.add("verified-role");
        return ids;   
    }

	/* (non-Javadoc)
	 * @see com.sun.xacml.finder.AttributeFinderModule#findAttribute(java.net.URI, java.net.URI, java.net.URI, java.net.URI, com.sun.xacml.EvaluationCtx, int)
	 */
	public EvaluationResult findAttribute(URI attributeType, URI attributeId, String issuer, URI subjectCategory,
			EvaluationCtx context, int designatorType) {
		Logger.info("In VerifySubjectRoleModule...");

		EvaluationResult subject = null;
		String subjectId = null;
		String assertedRoleValue = null;
		EvaluationResult assertedRole = null;

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
			assertedRole = context.getAttribute(new URI(StringAttribute.identifier),
					new URI("role"), issuer, subjectCategory);
			
			if (assertedRole.getAttributeValue().isBag()) {
				BagAttribute attr2 = (BagAttribute) assertedRole.getAttributeValue();
				for (Iterator<?> iterator2 = attr2.iterator(); iterator2.hasNext();) {
					AttributeValue val2 = (AttributeValue) iterator2.next();
					assertedRoleValue = val2.encode();

					break;
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		
		ArrayList<String> rs = null;
		try {
			rs = Util.readRoles(subjectId);
		} catch (Exception e) {
			Logger.error(e);
		}

		List<AttributeValue> attributeValues = new ArrayList<AttributeValue>();
		for (int i = 0; i < rs.size(); i++) {
			if(rs.get(i).equals(assertedRoleValue)){
				attributeValues.add(new StringAttribute(rs.get(i)));
				Logger.debug("Just added {} to string bag", rs.get(i));
			}
		}
		return new EvaluationResult(new BagAttribute(attributeType, attributeValues));
	}

}

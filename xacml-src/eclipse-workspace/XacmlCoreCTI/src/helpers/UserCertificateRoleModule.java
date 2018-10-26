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

import core.Utils;
import xacml.Util;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;

/**
 * Extension of Sun's AttributeFinderModule. This module takes in a X509 certificate from the XACML Request and looksup the users role based on the certs email. 
 * @author 
 * Justin Fleming - fleminjr@critical.com
 * Jeremy Fields - fieldsjd@critical.com
 *
 */
public class UserCertificateRoleModule extends AttributeFinderModule {

    private URI defaultId;

    public UserCertificateRoleModule() {
        try {
        	defaultId = new URI("certificate");
        } catch (URISyntaxException e) { /*ignore*/ }
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
        ids.add("cert-email-role");
        return ids;   
    }

	
	@Override
	public boolean isDesignatorSupported() {
		return true;
	}

	@Override
	public EvaluationResult findAttribute(URI attributeType, URI attributeId, String issuer, URI subjectCategory,
			EvaluationCtx context) {
		
		Logger.debug("In UserCertificateRoleModule for: " + attributeId.toString());
		
		String email = null;
		try {
			// Get host ip from XACML request
			EvaluationResult er = context.getAttribute(new URI(StringAttribute.identifier), defaultId, issuer, subjectCategory);
			String cert_s = null;
			if (er.getAttributeValue().isBag()) {
				BagAttribute attr = (BagAttribute) er.getAttributeValue();
				for (Iterator<?> iterator = attr.iterator(); iterator.hasNext();) {
					cert_s = ((AttributeValue) iterator.next()).encode();
					break;
				}
			}
			X509Certificate x = Utils.loadNonExpiredCertificate(Utils.decodeBase64(cert_s));
			if (x == null){
				Logger.error("Error loading certificate, or certificate is not CURRENT");
				return new EvaluationResult(new BagAttribute(attributeType, new ArrayList<AttributeValue>()));
			}
			email = Utils.extractEmail(x.getSubjectDN());
		} catch (Exception e) {
			Logger.error(e);
		}

		ArrayList<String> rs = null;
		try {
			rs = Util.readRoles(email);
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
package helpers;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml2.core.impl.AttributeStatementBuilder;
import org.opensaml.saml2.core.impl.AudienceBuilder;
import org.opensaml.saml2.core.impl.ConditionsBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.SubjectBuilder;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.Signer;
import org.pmw.tinylog.Logger;

import core.Utils;
import xacml.PropTypes;
import xacml.Property;
import xacml.SignatureHelper;

public class CreateInsuranceCertificate {
	static XMLObjectBuilderFactory bf;

	public static void main(String[] args) throws Exception {

		// demo: create an assertion
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
		DateTime notBefore = new DateTime(df.parse("2017-07-11T10:25:00"));
		DateTime notOnOrAfter = new DateTime(df.parse("2018-07-11T10:24:59"));

		ArrayList<Property> props = new ArrayList<Property>();
		props.add(new Property("prop1", "prop1: prop1 is an arbitrary property used for demonstration. This specific property asserts that the file in question contains the contents 'dog\\n'", PropTypes.INSURED));
		String companyName="nobody.systems";
		String agentName="Jeremy Fields";
		String address="123 Fake St";
		String phone="555-555-5555";
		String email="jeremy@domain.com";
		ArrayList<String> policyConditions = new ArrayList<String>();
		policyConditions.add("This policy applies whereto hence under hereforth only if the insured is an employee of Critical Technologies Inc.");
		int amount=5;
		String fullPolicy="https://ctidev4.critical.com/certs/policy.pdf";
		Assertion ass = makeAssertion("nobody.systems", "ee8ca7a80229e38588e5a1062a2320c6c372a097", "sha1", "www.critical.com", notBefore, notOnOrAfter,
				"guard.txt", "A guard file used for demonstration purposes", 4, "v1.0", props, companyName, agentName, address, phone, email, policyConditions, amount, fullPolicy);
		System.out.println(new SignatureHelper().marshall(ass));
	}

	public static Assertion makeAssertion(String issuer_str, String hash, String hashAlgo, String audience, DateTime notBefore, DateTime notOnOrAfter, String name_str,
			String description, int length, String version, ArrayList<Property> props, String companyName, String agentName, String address, String phone, String email, ArrayList<String> policyConditions,int amount,String fullPolicy)
			throws ParserConfigurationException, JAXBException, UnmarshallingException, XMLParserException, ConfigurationException, IOException {
		try {
			DefaultBootstrap.bootstrap();
		} catch (ConfigurationException e) {
			Logger.error(e);
		}
		bf = Configuration.getBuilderFactory();
		// Assertion (Root)
		AssertionBuilder assertionBuilder = (AssertionBuilder) bf.getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
		Assertion assertion = assertionBuilder.buildObject();
		assertion.setID("_" + Utils.generateRandomString(10) + System.currentTimeMillis());

		// Issuer
		IssuerBuilder issuerBuilder = (IssuerBuilder) bf.getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
		Issuer issuer = issuerBuilder.buildObject();
		issuer.setValue(issuer_str);

		// Subject
		SubjectBuilder subjectBuilder = (SubjectBuilder) bf.getBuilder(Subject.DEFAULT_ELEMENT_NAME);
		Subject subject = subjectBuilder.buildObject();
		NameIDBuilder nameIdBuilder = (NameIDBuilder) bf.getBuilder(NameID.DEFAULT_ELEMENT_NAME);
		NameID name = nameIdBuilder.buildObject();
		name.setNameQualifier(hashAlgo);
		name.setValue(hash);
		subject.setNameID(name);

		// Conditions
		ConditionsBuilder conditionsBuilder = (ConditionsBuilder) bf.getBuilder(Conditions.DEFAULT_ELEMENT_NAME);
		SAMLObjectBuilder<?> arBuilder = (SAMLObjectBuilder<?>) bf.getBuilder(AudienceRestriction.DEFAULT_ELEMENT_NAME);
		AudienceBuilder audBuilder = (AudienceBuilder) bf.getBuilder(Audience.DEFAULT_ELEMENT_NAME);
		Conditions conditions = (Conditions) conditionsBuilder.buildObject();
		AudienceRestriction ar = (AudienceRestriction) arBuilder.buildObject();
		Audience aud = (Audience) audBuilder.buildObject();
		aud.setAudienceURI(audience);
		ar.getAudiences().add(aud);
		conditions.getAudienceRestrictions().add(ar);
		if (notBefore == null)
			notBefore = new DateTime(new Date());
		conditions.setNotBefore(notBefore);
		conditions.setNotOnOrAfter(notOnOrAfter);

		// AttributeStatement
		AttributeStatementBuilder asBuilder = (AttributeStatementBuilder) bf.getBuilder(AttributeStatement.DEFAULT_ELEMENT_NAME);
		AttributeStatement as = (AttributeStatement) asBuilder.buildObject();

		// Attributes
		as.getAttributes().add(makeAttValue(props, name_str, description, Integer.toString(length), version, companyName, agentName, address, phone, email, policyConditions, amount, fullPolicy));

		assertion.setIssuer(issuer);
		assertion.setConditions(conditions);
		assertion.setSubject(subject);
		assertion.setIssueInstant(new DateTime());
		assertion.getAttributeStatements().add(as);

		SignatureHelper help = new SignatureHelper();
		Signature sig = help.makeSignature(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1, help.createCredential());
		assertion.setSignature(sig);

		List<Signature> signatureList = new ArrayList<Signature>();
		signatureList.add(sig);

		// Marshall and Sign
		MarshallerFactory marshallerFactory = org.opensaml.xml.Configuration.getMarshallerFactory();
		Marshaller marshaller = marshallerFactory.getMarshaller(Assertion.DEFAULT_ELEMENT_NAME);
		try {
			marshaller.marshall(assertion);
			org.apache.xml.security.Init.init();
			Signer.signObjects(signatureList);
		} catch (Exception e) {
			Logger.error(e);
		}

		return assertion;
	}

	private static Attribute makeAttValue(ArrayList<Property> props, String name, String description, String length, String version, String companyName, String agentName, String address, String phone, String email, ArrayList<String> policyConditions,int amount,String fullPolicy) throws ParserConfigurationException {
		SAMLObjectBuilder<?> attBuilder = (SAMLObjectBuilder<?>) bf.getBuilder(Attribute.DEFAULT_ELEMENT_NAME);
		Attribute att = (Attribute) attBuilder.buildObject();
		att.setName("insurance-info");

		@SuppressWarnings("unchecked")
		XMLObjectBuilder<XSAny> xsAnyBuilder = (XMLObjectBuilder<XSAny>) bf.getBuilder(XSAny.TYPE_NAME);

		XSAny attVal = xsAnyBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);

		XSAny sname_elem = xsAnyBuilder.buildObject(null, "name", null);
		sname_elem.setTextContent(name);

		XSAny sdesc_elem = xsAnyBuilder.buildObject(null, "description", null);
		sdesc_elem.setTextContent(description);

		XSAny length_elem = xsAnyBuilder.buildObject(null, "length", null);
		length_elem.setTextContent(length);

		XSAny vers_elem = xsAnyBuilder.buildObject(null, "version", null);
		vers_elem.setTextContent(version);

		XSAny provider_elem = xsAnyBuilder.buildObject(null, "provider", null);
		XSAny c_name_elem = xsAnyBuilder.buildObject(null, "company_name", null);
		c_name_elem.setTextContent(companyName);
		provider_elem.getUnknownXMLObjects().add(c_name_elem);
		
		XSAny a_name_elem = xsAnyBuilder.buildObject(null, "agent_name", null);
		a_name_elem.setTextContent(agentName);
		provider_elem.getUnknownXMLObjects().add(a_name_elem);
		
		XSAny address_elem = xsAnyBuilder.buildObject(null, "address", null);
		address_elem.setTextContent(address);
		provider_elem.getUnknownXMLObjects().add(address_elem);
		
		XSAny phone_elem = xsAnyBuilder.buildObject(null, "phone", null);
		phone_elem.setTextContent(phone);
		provider_elem.getUnknownXMLObjects().add(phone_elem);

		XSAny email_elem = xsAnyBuilder.buildObject(null, "email", null);
		email_elem.setTextContent(email);
		provider_elem.getUnknownXMLObjects().add(email_elem);
		
		XSAny policy_elem = xsAnyBuilder.buildObject(null, "policy", null);
		XSAny props_elem = xsAnyBuilder.buildObject(null, "properties", null);
		for (Property prop : props) {
			XSAny property_elem = xsAnyBuilder.buildObject(null, "property", null);
			
			XSAny name_elem = xsAnyBuilder.buildObject(null, "name", null);
			name_elem.setTextContent(prop.propertyName);
			XSAny desc_elem = xsAnyBuilder.buildObject(null,"description", null);
			desc_elem.setTextContent(prop.propertyValue);
			property_elem.getUnknownXMLObjects().add(name_elem);
			property_elem.getUnknownXMLObjects().add(desc_elem);
			
			props_elem.getUnknownXMLObjects().add(property_elem);
		}
		policy_elem.getUnknownXMLObjects().add(props_elem);
		
		XSAny conds_elem = xsAnyBuilder.buildObject(null, "conditions", null);
		for (String cond : policyConditions) {
			XSAny cond_elem = xsAnyBuilder.buildObject(null, "condition", null);
			cond_elem.setTextContent(cond);
			conds_elem.getUnknownXMLObjects().add(cond_elem);
		}
		policy_elem.getUnknownXMLObjects().add(conds_elem);
		XSAny amount_elem = xsAnyBuilder.buildObject(null, "amount_insured", null);
		amount_elem.setTextContent(Integer.toString(amount));
		policy_elem.getUnknownXMLObjects().add(amount_elem);

		XSAny fp_elem = xsAnyBuilder.buildObject(null, "full_policy", null);
		fp_elem.setTextContent(fullPolicy);
		policy_elem.getUnknownXMLObjects().add(fp_elem);
		
		attVal.getUnknownXMLObjects().add(sname_elem);
		attVal.getUnknownXMLObjects().add(sdesc_elem);
		attVal.getUnknownXMLObjects().add(length_elem);
		attVal.getUnknownXMLObjects().add(vers_elem);
		attVal.getUnknownXMLObjects().add(provider_elem);
		attVal.getUnknownXMLObjects().add(policy_elem);
		
		

		att.getAttributeValues().add(attVal);

		return att;

	}

}

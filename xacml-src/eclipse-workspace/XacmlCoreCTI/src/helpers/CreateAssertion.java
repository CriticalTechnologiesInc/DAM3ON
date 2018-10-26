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
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.Assertion;
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

public class CreateAssertion {

	static XMLObjectBuilderFactory bf;
	public static void main(String[] args) throws Exception {
		
		// demo: create an assertion
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
		DateTime notBefore = new DateTime(df.parse("2017-07-11T10:25:00"));
		DateTime notOnOrAfter = new DateTime(df.parse("2018-07-11T10:24:59"));
		
		ArrayList<Property> props = new ArrayList<Property>();

//		props.add(new Property("no failure", "https://ctidev4.critical.com/certs/nofail.thy", PropTypes.PROOF));
//		props.add(new Property("uses brackets", "none", PropTypes.FUCKYOU));
		
//		props.add(new Property("no failure", "https://ctidev4.critical.com/certs/c-spec.xml", PropTypes.REF));
//		props.add(new Property("no infinite loops", "https://ctidev4.critical.com/certs/noinfiniteloops.thy", PropTypes.PROOF));
//		
//		props.add(new Property("no failure", "https://ctidev4.critical.com/certs/highlevel-spec.xml", PropTypes.REF));
//		props.add(new Property("no infinite loops", "https://ctidev4.critical.com/certs/highlevel-spec.xml", PropTypes.REF));
//		props.add(new Property("no integer overflow", "https://ctidev4.critical.com/certs/c-spec.xml", PropTypes.REF));
//		props.add(new Property("no segfault", "https://ctidev4.critical.com/certs/segfault.thy", PropTypes.PROOF));
		
//		props.add(new Property("will terminate", "none", PropTypes.FUCKYOU));
//		props.add(new Property("prop1", "https://ctidev4.critical.com/certs/prop1.thy", PropTypes.PROOF));
//		props.add(new Property("prop2", "https://ctidev4.critical.com/certs/cert2.xml", PropTypes.REF));
		props.add(new Property("prop1", "https://ctidev4.critical.com/certs/p1insurance.xml", PropTypes.INSURED));
//		props.add(new Property("prop2", null, PropTypes.FUCKYOU));
//		props.add(new Property("prop3", "https://ctidev4.critical.com/certs/p3proof.xml", PropTypes.PROOF));
		
		
//		Assertion ass = makeAssertion("nobody.systems", "hashofclangspeceefaddeadbeefaddeadbeefad", "sha256", "www.critical.com", notBefore, notOnOrAfter, "C Language Specification", "Specification of the C Language", props, 512, "v2");
//		Assertion ass = makeAssertion("nobody.systems", "hashofspecdeadbeefaddeadbeefaddeadbeefad", "sha256", "www.critical.com", notBefore, notOnOrAfter, "HighLevelSpecification", "SYR High level spec", props, 512, "v.05");
//		Assertion ass = makeAssertion("nobody.systems", "sablecertificatedeaddeadbeefbeefbeefbeef", "sha256", "www.critical.com", notBefore, notOnOrAfter, "SABLE", "syracuse assured bootloader executive", props, 512, "v.05");
		Assertion ass = makeAssertion("nobody.systems", "ee8ca7a80229e38588e5a1062a2320c6c372a097", "sha1", "www.critical.com", notBefore, notOnOrAfter, "guard.txt", "This certificate lists some properties of guard.txt", props, 4, "v1.0");
		System.out.println(new SignatureHelper().marshall(ass));
	}
	
	public static Assertion makeAssertion(String issuer_str, String hash, String hashAlgo, String audience, DateTime notBefore, DateTime notOnOrAfter,
										  String name_str, String description, ArrayList<Property> props, int length, String version ) throws ParserConfigurationException, JAXBException, UnmarshallingException, XMLParserException, ConfigurationException, IOException{
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
		if(notBefore == null)
			notBefore = new DateTime(new Date());
		conditions.setNotBefore(notBefore);
		conditions.setNotOnOrAfter(notOnOrAfter);
		
		// AttributeStatement
		AttributeStatementBuilder asBuilder = (AttributeStatementBuilder) bf.getBuilder(AttributeStatement.DEFAULT_ELEMENT_NAME);
		AttributeStatement as = (AttributeStatement)asBuilder.buildObject();

		// Attributes
		as.getAttributes().add(makeAttValue(props, name_str, description, Integer.toString(length),version));

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

	
	private static Attribute makeAttValue(ArrayList<Property> props, String name, String description, String length, String version) throws ParserConfigurationException{
		SAMLObjectBuilder<?> attBuilder = (SAMLObjectBuilder<?>) bf.getBuilder(Attribute.DEFAULT_ELEMENT_NAME);
		Attribute att = (Attribute)attBuilder.buildObject();
		att.setName("cert-info");
		
		@SuppressWarnings("unchecked")
		XMLObjectBuilder<XSAny> xsAnyBuilder = (XMLObjectBuilder<XSAny>)bf.getBuilder(XSAny.TYPE_NAME);
		
		XSAny attVal = xsAnyBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
		
		XSAny name_elem = xsAnyBuilder.buildObject(null, "name", null);
		name_elem.setTextContent(name);
		
		XSAny desc_elem = xsAnyBuilder.buildObject(null, "description", null);
		desc_elem.setTextContent(description);
		
		XSAny length_elem = xsAnyBuilder.buildObject(null, "length", null);
		length_elem.setTextContent(length);
		
		XSAny vers_elem = xsAnyBuilder.buildObject(null, "version", null);
		vers_elem.setTextContent(version);
		
		XSAny info_elem = xsAnyBuilder.buildObject(null, "property_info", null);
		
		for(Property prop : props){
			XSAny property_elem = xsAnyBuilder.buildObject(null, "property", null);
			property_elem.setTextContent(prop.propertyName);
			XSAny entry_elem = xsAnyBuilder.buildObject(null, "entry", null);
			
			entry_elem.getUnknownXMLObjects().add(property_elem);
			if(prop.type != PropTypes.FUCKYOU){
				XSAny proof_elem = xsAnyBuilder.buildObject(null, prop.type.type(), null);
				proof_elem.setTextContent(prop.propertyValue);
				entry_elem.getUnknownXMLObjects().add(proof_elem);
			}
			info_elem.getUnknownXMLObjects().add(entry_elem);
		}
		
		
		attVal.getUnknownXMLObjects().add(name_elem);
		attVal.getUnknownXMLObjects().add(desc_elem);
		attVal.getUnknownXMLObjects().add(length_elem);
		attVal.getUnknownXMLObjects().add(vers_elem);
		attVal.getUnknownXMLObjects().add(info_elem);
		
		att.getAttributeValues().add(attVal);
		

		return att;

	}
	
}

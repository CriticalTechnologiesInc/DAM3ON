package helpers;

import java.io.IOException;

import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.SubjectBuilder;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.XMLHelper;
import org.pmw.tinylog.Logger;
import org.w3c.dom.Element;


/**
 * Helper class to create a SAML Assertion
 * @author 
 * Justin Fleming - fleminjr@critical.com
 * Jeremy Fields - fieldsjd@critical.com
 */
public class AssertionGenerator {
	/**
	 * Instantiate a global XMLObjectBuilderFactory to null 
	 */
	private XMLObjectBuilderFactory builderFactory = null;

	/**
	 * Default constructor. Calls the bootstrap method,
	 * and instantiates the builderFactory object
	 */
	public AssertionGenerator(){
		try {
			DefaultBootstrap.bootstrap();
		} catch (ConfigurationException e) {
			Logger.error(e);
		}
		builderFactory = Configuration.getBuilderFactory();
	}
	
	/**
	 * This is the function that is used to instantiate a SAML Assertion
	 * 
	 * @param issuerValue String of the issuer used to create an assertion with
	 * @param subjectNameId String of the subject name used in the SAML headers
	 * @param subjectSchemaLocation String representing the location of the schema
	 * @return Assertion object
	 */
	public Assertion makeAssertion(String issuerValue,String subjectNameId,String subjectSchemaLocation){
		// Get assertion builder
		AssertionBuilder assertionBuilder = (AssertionBuilder) builderFactory.getBuilder(Assertion.DEFAULT_ELEMENT_NAME);

		// Create assertion
		Assertion assertion = assertionBuilder.buildObject();
		
		assertion.setIssuer(this.makeIssuer(issuerValue));
		assertion.setSubject(this.makeSubject(subjectNameId,subjectSchemaLocation));
		assertion.setIssueInstant(new DateTime());
		
		return assertion;
	}
	
	/**
	 * This method creates a Subject object used to put inside an Assertion 
	 * 
	 * @param name String representing the Subject of the SAML document
	 * @param schemaLocation String of the location to the schema (URL/filepath??)
	 * @return Subject object
	 */
	public Subject makeSubject(String name, String schemaLocation){
		// Get subject builder
		SubjectBuilder subjectBuilder = (SubjectBuilder) builderFactory.getBuilder(Subject.DEFAULT_ELEMENT_NAME);

		// Create Subject
		Subject subject = subjectBuilder.buildObject();

		// Set things for subject
		subject.setNameID(this.makeNameID(name));
		subject.setSchemaLocation(schemaLocation);
		
		return subject;
	}
	
	/**
	 * This method creates a NameID object used to construct a SAML document
	 * 
	 * @param nameId String of the value to be set for the NameID in SAML
	 * @return NameID object
	 */
	public NameID makeNameID(String nameId){
		// Get NameIDBuilder and build NameID for subject
		NameIDBuilder nameIdBuilder = (NameIDBuilder) builderFactory.getBuilder(NameID.DEFAULT_ELEMENT_NAME);
		NameID name = nameIdBuilder.buildObject();
		name.setValue(nameId);
		
		return name;
	}
	
	/**
	 * This method creates an Issuer object used to construct a SAML document
	 * 
	 * @param issuerValue String of the Issuer in SAML
	 * @return Issuer object
	 */
	public Issuer makeIssuer(String issuerValue){
		// Get IssuerBuilder and create Issuer for assertion
		IssuerBuilder issuerBuilder = (IssuerBuilder) builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
		Issuer issuer = issuerBuilder.buildObject();
		issuer.setValue(issuerValue);
		
		return issuer;
	}
	
	/**
	 * This method takes an Assertion object, and marshalls it to a string.
	 * AKA - Converts Assertion --> String
	 * 
	 * @param assertion Assertion object
	 * @return String
	 * @throws IOException 
	 */
	public String marshallToString(Assertion assertion) { 
		// Get marshaller factory
		MarshallerFactory marshallerFactory = Configuration
				.getMarshallerFactory();

		// Get assertion marshaller and marshall it
		Marshaller marshaller = marshallerFactory
				.getMarshaller(assertion);
		Element assertionElement = null;
		String xmlString = "";
		try {
			assertionElement = marshaller.marshall(assertion);
			xmlString =  XMLHelper.nodeToString(assertionElement);
		} catch (MarshallingException e) {
			Logger.error(e);
		}
		return xmlString;
	}
}

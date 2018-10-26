package xacml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionQueryType;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.security.Criteria;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.credential.KeyStoreCredentialResolver;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.criteria.UsageCriteria;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.opensaml.xml.signature.X509Certificate;
import org.opensaml.xml.signature.X509Data;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.opensaml.xml.signature.impl.KeyInfoBuilder;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.opensaml.xml.signature.impl.X509CertificateBuilder;
import org.opensaml.xml.signature.impl.X509DataBuilder;
import org.opensaml.xml.validation.ValidationException;
import org.pmw.tinylog.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import core.Config;

/**
 * 
 * Helper class to sign and verify SAML Request and Response Signatures
 * @author 
 * Justin Fleming - fleminjr@critical.com
 * Jeremy Fields - fieldsjd@critical.com
 */
public class SignatureHelper {
	/**
	 * Global variable to determine if we've already bootstrapped or not
	 */
	private boolean isBootStrapped = false;
	
//	ConfigHelper ch;
//
//	/**
//	 * Default constructor which initializes all relevant global variables that may be needed
//	 */
//	public SignatureHelper() {
//		ch = new ConfigHelper();
//	}

	public Object unmarshal(String xml) throws UnmarshallingException, XMLParserException, ConfigurationException{
		// Initialize the library
		DefaultBootstrap.bootstrap(); 
		 
		// Get parser pool manager
		BasicParserPool ppMgr = new BasicParserPool();
		ppMgr.setNamespaceAware(true);
		 
		// Parse metadata file
		Document inCommonMDDoc = ppMgr.parse(new ByteArrayInputStream(xml.getBytes()));
		Element metadataRoot = inCommonMDDoc.getDocumentElement();
		 
		// Get apropriate unmarshaller
		UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
		Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(metadataRoot);
		 
		// Unmarshall using the document root element, an EntitiesDescriptor in this case
		return unmarshaller.unmarshall(metadataRoot);
	}
	
	/**
	 * This is the outer 'public' method used to verify the Signature on a SAML Request.
	 * This method does many things, including: verifying that the SAML is properly formed (as in
	 * it adheres to the SAML schema), it compares digest values, and also verifies that the certificate
	 * contained in the SAML is one that we trust, by reading our own local keystore to see if it
	 * contains that certificate.
	 * 
	 * @param requestString String of the full, unaltered request
	 * @return Boolean
	 * @throws IOException
	 */
	public boolean verifyRequestSignature(String requestString) {
		
		
		XACMLAuthzDecisionQueryType xacmlAuthzDecisionQuery;
		try {
			doBootstrap();
			xacmlAuthzDecisionQuery = (XACMLAuthzDecisionQueryType) unmarshall(requestString); // TODO here
			
			if(validateSigProfile(xacmlAuthzDecisionQuery.getSignature())){
				Logger.info("Signature passed SAMLProfileValidator");
				
				String requestIssuerList[] = Config.Saml.request_issuers.toArray(new String[0]);
				
				
				if(Arrays.asList(requestIssuerList).contains(xacmlAuthzDecisionQuery.getIssuer().getValue())){
					Logger.info("Signature issuer string confirmed");
					
					if(trustedRequestSignature(xacmlAuthzDecisionQuery)){
						Logger.info("Signature is trusted");
						Logger.info("Passed all three, returning true for verifyRequestSignature");
						return true;	
					} else {
						Logger.warn("The signature is not trusted as determined by TrustEngine");
						return false;
					}
				} else {
					Logger.warn("The signature issuer string was not as expected");
					return false;
				}
			} else {
				Logger.warn("The signature did not pass SAMLProfileValidator");
				return false;
			}
		} catch(Exception e){
			Logger.error(e);
			return false;
		}
	}
	
	/**
	 * This is the outer 'public' method used to verify the Signature on a SAML Response.
	 * This method does many things, including: verifying that the SAML is properly formed (as in
	 * it adheres to the SAML schema, it compares digest values, and also verifies that the certificate
	 * contained in the SAML is one that we trust, by reading our own local keystore to see if it
	 * contains that certificate.
	 * 
	 * @param responseString String of the full, unaltered response
	 * @return Boolean
	 * @throws IOException
	 */
	public boolean verifyResponseSignature(String responseString) {
		
		Response response;
		
		try {
			doBootstrap();
			response = (Response) unmarshall(responseString);
			
			if(validateSigProfile(response.getSignature())){
				Logger.info("Signature passed SAMLProfileValidator");
				
				if(response.getIssuer().getValue().equals(Config.Saml.response_issuers.get(0))){
					Logger.info("Signature issuer string confirmed");
					
					if(trustedResponseSignature(response)){
						Logger.info("Signature is trusted");
						Logger.info("Passed all three, returning true for verifyResponseSignature");
						return true;
								
					} else {
						Logger.warn("The signature is not trusted as determined by TrustEngine");
						return false;
					}
				} else {
					Logger.warn("The signature issuer string was not as expected");
					return false;
				}
			} else {
				Logger.warn("The signature did not pass SAMLProfileValidator");
				return false;
			}
		} catch(Exception e){
			Logger.error(e);
			return false;
		}
	}
	
	/**
	 * This is the outer 'public' method used to verify the Signature on a SAML Response.
	 * This method does many things, including: verifying that the SAML is properly formed (as in
	 * it adheres to the SAML schema, it compares digest values, and also verifies that the certificate
	 * contained in the SAML is one that we trust, by reading our own local keystore to see if it
	 * contains that certificate.
	 * 
	 * @param responseString String of the full, unaltered response
	 * @return Boolean
	 * @throws IOException
	 */
	public boolean verifyAssertionSignature(String assString) {
		
		Assertion ass;
		
		try {
			doBootstrap();
			ass = (Assertion) unmarshall(assString);
			
			if(validateSigProfile(ass.getSignature())){
				Logger.info("Signature passed SAMLProfileValidator");
				
					if(trustedAssertionSignature(ass)){
						Logger.info("Signature is trusted");
						Logger.info("Passed all three, returning true for verifyResponseSignature");
						return true;
								
					} else {
						Logger.warn("The signature is not trusted as determined by TrustEngine");
						return false;
					}

			} else {
				Logger.warn("The signature did not pass SAMLProfileValidator");
				return false;
			}
		} catch(Exception e){
			Logger.error(e);
			return false;
		}
	}
	
	/**
	 * This is a method that verifies that the SAML is valid (adheres to SAML schema)
	 * 
	 * @param signature Signature object, which is the entire Signature tag in SAML
	 * @return Boolean
	 */
	private boolean validateSigProfile(Signature signature){
		SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
		try {
			profileValidator.validate(signature);
		} catch (ValidationException e) {
			// Indicates signature did not conform to SAML Signature profile
			Logger.error(e);
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * This method ensures that the provided digest value in the SAML matches it's calculate digest,
	 * and then reads in our own local keystore, to verify that the provided certificate is one that's
	 * in our local keystore (and therefore one that we trust)
	 * 
	 * @param request XACMLAuthzDecisionQueryType object of the request
	 * @return boolean
	 * @throws IOException
	 * @see "https://wiki.shibboleth.net/confluence/display/OpenSAML/OSTwoUserManJavaDSIG"
	 */
	private boolean trustedRequestSignature(XACMLAuthzDecisionQueryType request){
		KeyStore keystore = null;
		
		try {
			keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			FileInputStream inputStream = new FileInputStream(Config.Keystore.keystore_path);
			keystore.load(inputStream, Config.Keystore.store_pass.toCharArray());
			inputStream.close();
		} catch (Exception e) {
			System.out.println("HERE");
			Logger.error(e);
			return false;
		}
		
		Map<String, String> passwordMap = new HashMap<String, String>();
		
		KeyStoreCredentialResolver keyStoreCredResolver = new KeyStoreCredentialResolver(keystore, passwordMap);
		
		KeyInfoCredentialResolver keyInfoCredResolver = Configuration.getGlobalSecurityConfiguration()
				.getDefaultKeyInfoCredentialResolver();
		
		ExplicitKeySignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(keyStoreCredResolver,
				keyInfoCredResolver);

		SignatureTrustEngine sigTrustEngine = trustEngine;
		CriteriaSet criteriaSet = new CriteriaSet();
		criteriaSet.add(new EntityIDCriteria(request.getIssuer().getValue()));
		criteriaSet.add(new UsageCriteria(UsageType.SIGNING));
		
		try {
			if (!sigTrustEngine.validate(request.getSignature(), criteriaSet)) {
				throw new Exception("Signature was either invalid or signing key could not be established as trusted");
			}
			return true;
		} catch (Exception e) {
			// Indicates processing error evaluating the signature
			Logger.error(e);
			return false;
		}
		
	}

	/**
	 * 
	 * This method ensures that the provided digest value in the SAML matches it's calculate digest,
	 * and then reads in our own local keystore, to verify that the provided certificate is one that's
	 * in our local keystore (and therefore one that we trust)
	 * @param response Response object of the response
	 * @return boolean
	 * @throws IOException
	 * @see "https://wiki.shibboleth.net/confluence/display/OpenSAML/OSTwoUserManJavaDSIG"
	 */
	private boolean trustedResponseSignature(Response response) {
		KeyStore keystore = null;
		
		try {
			keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			
			FileInputStream inputStream = new FileInputStream(Config.Keystore.keystore_path);
			keystore.load(inputStream, Config.Keystore.store_pass.toCharArray());
			inputStream.close();
		} catch (Exception e) {
			Logger.error(e);
			return false;
		}

		Map<String, String> passwordMap = new HashMap<String, String>();
		KeyStoreCredentialResolver keyStoreCredResolver = new KeyStoreCredentialResolver(keystore, passwordMap);
		
		KeyInfoCredentialResolver keyInfoCredResolver = Configuration.getGlobalSecurityConfiguration()
				.getDefaultKeyInfoCredentialResolver();
		
		ExplicitKeySignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(keyStoreCredResolver,
				keyInfoCredResolver);

		SignatureTrustEngine sigTrustEngine = trustEngine;
		CriteriaSet criteriaSet = new CriteriaSet();
		criteriaSet.add(new EntityIDCriteria(response.getIssuer().getValue()));
		criteriaSet.add(new UsageCriteria(UsageType.SIGNING));
		
		try {
			if (!sigTrustEngine.validate(response.getSignature(), criteriaSet)) {
				throw new Exception("Signature was either invalid or signing key could not be established as trusted");
			}
			return true;
		} catch (Exception e) {
			// Indicates processing error evaluating the signature
			Logger.error(e);
			return false;
		}
	}
	
	/**
	 * 
	 * This method ensures that the provided digest value in the SAML matches it's calculate digest,
	 * and then reads in our own local keystore, to verify that the provided certificate is one that's
	 * in our local keystore (and therefore one that we trust)
	 * @param assertion Assertion object of the assertion
	 * @return boolean
	 * @throws IOException
	 * @see "https://wiki.shibboleth.net/confluence/display/OpenSAML/OSTwoUserManJavaDSIG"
	 */
	private boolean trustedAssertionSignature(Assertion ass) {
		KeyStore keystore = null;
		
		try {
			keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			
			FileInputStream inputStream = new FileInputStream(Config.Keystore.keystore_path);
			keystore.load(inputStream, Config.Keystore.store_pass.toCharArray());
			inputStream.close();
		} catch (Exception e) {
			Logger.error(e);
			return false;
		}

		Map<String, String> passwordMap = new HashMap<String, String>();
		KeyStoreCredentialResolver keyStoreCredResolver = new KeyStoreCredentialResolver(keystore, passwordMap);
		
		KeyInfoCredentialResolver keyInfoCredResolver = Configuration.getGlobalSecurityConfiguration()
				.getDefaultKeyInfoCredentialResolver();
		
		ExplicitKeySignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(keyStoreCredResolver,
				keyInfoCredResolver);

		SignatureTrustEngine sigTrustEngine = trustEngine;
		CriteriaSet criteriaSet = new CriteriaSet();
		criteriaSet.add(new EntityIDCriteria(ass.getIssuer().getValue()));
		criteriaSet.add(new UsageCriteria(UsageType.SIGNING));
		
		try {
			if (!sigTrustEngine.validate(ass.getSignature(), criteriaSet)) {
				throw new Exception("Signature was either invalid or signing key could not be established as trusted");
			}
			return true;
		} catch (Exception e) {
			// Indicates processing error evaluating the signature
			Logger.error(e);
			return false;
		}
	}

	/**********************************************************************************************************************************************************/
	/**********************************************************************************************************************************************************/

	/**
	 * This method creates a signature to be used when creating some SAML, based off of a local key.
     *
	 * @param signatureAlgorithm String representing the algorithm of: the key we're using, and how to create the digest.
	 * This should be a value in the SignatureConstants object. An example would be: SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1
	 * @param cred X509Credential object that represents our own local keystore key that we'll use to create
	 * signatures
	 * @return Signature
	 * @throws IOException
	 */
	public Signature makeSignature(String signatureAlgorithm, X509Credential cred){
		XMLObjectBuilderFactory builderFactory0 = Configuration.getBuilderFactory();
		SignatureBuilder builder0 = (SignatureBuilder) builderFactory0.getBuilder(Signature.DEFAULT_ELEMENT_NAME);
		Signature signature = builder0.buildObject();

		
		signature.setSigningCredential(cred);
		signature.setSignatureAlgorithm(signatureAlgorithm);
		signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

		XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
		KeyInfoBuilder builder = (KeyInfoBuilder) builderFactory.getBuilder(KeyInfo.DEFAULT_ELEMENT_NAME);
		KeyInfo keyInfo = builder.buildObject();

		XMLObjectBuilderFactory builderFactory2 = Configuration.getBuilderFactory();
		X509DataBuilder builder2 = (X509DataBuilder) builderFactory2.getBuilder(X509Data.DEFAULT_ELEMENT_NAME);
		X509Data x509Data = builder2.buildObject();

		XMLObjectBuilderFactory builderFactory3 = Configuration.getBuilderFactory();
		X509CertificateBuilder builder3 = (X509CertificateBuilder) builderFactory3
				.getBuilder(X509Certificate.DEFAULT_ELEMENT_NAME);
		X509Certificate x509certificate = builder3.buildObject();

		String value = null;
		try {
			value = org.apache.xml.security.utils.Base64.encode(cred.getEntityCertificate().getEncoded());
		} catch (Exception e) {
			Logger.error(e);
		}
		x509certificate.setValue(value);
		x509Data.getX509Certificates().add(x509certificate);
		keyInfo.getX509Datas().add(x509Data);
		signature.setKeyInfo(keyInfo);

		return signature;
	}

	/**
	 * This method creates and returns a X509Credential object. This object is created by reading the
	 * configuration file, to obtain the relevant information to read in a key from a 'jks' keystore.
	 * 
	 * 
	 * @return X509Credential
	 * @throws IOException
	 */
	public X509Credential createCredential() {
		KeyStore keystore = null;
		
		try {
			keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			
			FileInputStream inputStream = new FileInputStream(Config.Keystore.keystore_path);
			keystore.load(inputStream, Config.Keystore.store_pass.toCharArray());
			inputStream.close();
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}

		Map<String, String> passwordMap = new HashMap<String, String>();

		passwordMap.put(Config.Keystore.keystore_alias, Config.Keystore.key_pass);
		


		KeyStoreCredentialResolver resolver = new KeyStoreCredentialResolver(keystore, passwordMap);

		Criteria criteria = new EntityIDCriteria(Config.Keystore.keystore_alias);
		CriteriaSet criteriaSet = new CriteriaSet(criteria);

		X509Credential credential = null;
		try {
			credential = (X509Credential) resolver.resolveSingle(criteriaSet);
		} catch (Exception e) {
			Logger.error(e);
		}
		return credential;
	}

	/**
	 * Runs default bootstrapping as dictated by: org.opensaml.DefaultBootstrap;
	 * @throws ConfigurationException 
	 *
	 * @throws IOException
	 */
	private void doBootstrap() throws ConfigurationException {
		if (!isBootStrapped) {
			DefaultBootstrap.bootstrap();
			isBootStrapped = true;
		}
	}

	/**
	 * This method unmarshalls a xml String to a XMLObject
	 * 
	 * @param xmlString String of some XML to be unmarshalled to an XMLObject
	 * @return XMLObject || null
	 * @throws IOException
	 */
	public XMLObject unmarshall(String xmlString) {
		Unmarshaller unmarshaller;
		try {
			doBootstrap();
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = docBuilder.parse(new ByteArrayInputStream(xmlString.trim().getBytes()));
			Element element = document.getDocumentElement();
			UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
			unmarshaller = unmarshallerFactory.getUnmarshaller(element);
			return unmarshaller.unmarshall(element);
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
	}

	/**
	 * This function marshall's a XMLObject to a String
	 * 
	 * @param xmlObject XMLObject to be marshalled
	 * @return String || null
	 * @throws IOException
	 */

	public String marshall(XMLObject xmlObject) throws IOException {
		try {
			doBootstrap();
			System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
					"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");

			MarshallerFactory marshallerFactory = org.opensaml.xml.Configuration.getMarshallerFactory();
			Marshaller marshaller = marshallerFactory.getMarshaller(xmlObject);

			Element element = marshaller.marshall(xmlObject);

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
			LSSerializer writer = impl.createLSSerializer();
			LSOutput output = impl.createLSOutput();
			output.setByteStream(byteArrayOutputStream);
			writer.write(element, output);
			return byteArrayOutputStream.toString();
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
	}

}

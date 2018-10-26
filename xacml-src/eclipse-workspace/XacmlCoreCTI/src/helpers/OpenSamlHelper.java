package helpers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Statement;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.xacml.ctx.RequestType;
import org.opensaml.xacml.ctx.ResponseType;
import org.opensaml.xacml.profile.saml.SAMLProfileConstants;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionQueryType;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionStatementType;
import org.opensaml.xacml.profile.saml.impl.XACMLAuthzDecisionQueryTypeImplBuilder;
import org.opensaml.xacml.profile.saml.impl.XACMLAuthzDecisionStatementTypeImplBuilder;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.util.XMLHelper;
import org.pmw.tinylog.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.balana.ctx.ResponseCtx;

import core.CTIConstants;
import core.Config;
import core.PGPHelper;
import xacml.SignatureHelper;

public class OpenSamlHelper {

	public OpenSamlHelper(){
		try {
			// NOTE: if this ever causes "InputStream cannot be null" - you probably need openws.jar
			DefaultBootstrap.bootstrap(); // this is -REQUIRED- before doing anything w/ opensaml
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	private XACMLAuthzDecisionStatementType getAuthzDecisionStatement() throws ConfigurationException{
		XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
		XACMLAuthzDecisionStatementTypeImplBuilder xacmlAuthz = (XACMLAuthzDecisionStatementTypeImplBuilder) builderFactory.getBuilder(XACMLAuthzDecisionStatementType.TYPE_NAME_XACML30);
		XACMLAuthzDecisionStatementType objectXACMLAuthz = xacmlAuthz.buildObject(Statement.DEFAULT_ELEMENT_NAME,XACMLAuthzDecisionStatementType.TYPE_NAME_XACML30);

		return objectXACMLAuthz;
	}
	
	
	public String wrapAndSignRequest(String request) {
		try {
			RequestType reqt = makeRequestFromString(request);
			
			XACMLAuthzDecisionQueryType signedRequest = samlSignRequest(reqt);
			String samlWrappedRequestString = marshallToString(signedRequest);
			return samlWrappedRequestString;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * This method takes in a XACMLAuthzDecisionQueryType object and marshalls it to a String
	 * 
	 * @param xacmlQuery XACMLAuthzDecisionQueryType object
	 * @return String of the marshalled XACMLAuthzDecisionQueryType object
	 * @throws IOException 
	 */
	public String marshallToString(XACMLAuthzDecisionQueryType xacmlQuery) {
		// Get marshaller factory (openSAML)
		MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();

		// Get XACMLAuthzDecisionQueryType marshaller and marshall it back to a
		// org.w3c.dom.Element object
		
		Marshaller marshaller = marshallerFactory
				.getMarshaller(XACMLAuthzDecisionQueryType.DEFAULT_ELEMENT_NAME_XACML20);
		Element queryElement = null;
		String xmlString = "";
		try {
			queryElement = marshaller.marshall(xacmlQuery);
			xmlString = XMLHelper.nodeToString(queryElement); 
		} catch (MarshallingException e) {
			Logger.error(e);
		}
		return xmlString;
	}
	
	
	public String wrapAndSignRawResponse(ResponseCtx rtx) {
		try {
			XACMLAuthzDecisionStatementType ads = getAuthzDecisionStatement();
			
			ResponseType rest = makeResponseFromString(rtx.encode());
			ads.setResponse(rest);
			
			String signedResponse = samlSignResponse(ads);
			return signedResponse;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private XACMLAuthzDecisionQueryType samlSignRequest(RequestType req) {
		// Get builder and build object (openSAML)
		XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
		XACMLAuthzDecisionQueryTypeImplBuilder xacmlDecisionQueryBuilder = (XACMLAuthzDecisionQueryTypeImplBuilder) builderFactory
				.getBuilder(XACMLAuthzDecisionQueryType.DEFAULT_ELEMENT_NAME_XACML30);
		XACMLAuthzDecisionQueryType xacmlQuery = xacmlDecisionQueryBuilder.buildObject(
				SAMLProfileConstants.SAML20XACML30P_NS, XACMLAuthzDecisionQueryType.DEFAULT_ELEMENT_LOCAL_NAME,
				SAMLProfileConstants.SAML20XACMLPROTOCOL_PREFIX);

		IssuerBuilder issuerBuilder = (IssuerBuilder) builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
		Issuer issuer = issuerBuilder.buildObject();
		issuer.setValue(Config.Saml.request_issuers.get(0));
		
		// Set values for query
		xacmlQuery.setID(Config.Saml.request_id);
		xacmlQuery.setDestination(Config.Saml.request_destination);
		xacmlQuery.setIssuer(issuer);
		xacmlQuery.setVersion(SAMLVersion.VERSION_20);
		xacmlQuery.setRequest(req);
		xacmlQuery.setIssueInstant(new DateTime());

		SignatureHelper help = new SignatureHelper();

		Signature sig = help.makeSignature(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1, help.createCredential());

		xacmlQuery.setSignature(sig);

		List<Signature> signatureList = new ArrayList<Signature>();
		signatureList.add(sig);

		// Marshall and Sign
		MarshallerFactory marshallerFactory = org.opensaml.xml.Configuration.getMarshallerFactory();
		Marshaller marshaller = marshallerFactory
				.getMarshaller(XACMLAuthzDecisionQueryType.DEFAULT_ELEMENT_NAME_XACML30);
		try {
			marshaller.marshall(xacmlQuery);
			org.apache.xml.security.Init.init();
			Signer.signObjects(signatureList);
		} catch (Exception e) {
			Logger.error(e);
		}

		return xacmlQuery;
	}
	
	private String samlSignResponse(XACMLAuthzDecisionStatementType objectXACMLAuthz) {
		XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
		AssertionGenerator assertionGenerator = new AssertionGenerator();
		Assertion assertion = assertionGenerator.makeAssertion(Config.Saml.Assertion.issuer, Config.Saml.Assertion.subject_name_id,
				Config.Saml.Assertion.subject_schema_location);
		
		
		String resourceAttributeValue = "";
		NodeList attributes = null;
		NodeList cn = objectXACMLAuthz.getResponse().getResult().getDOM().getElementsByTagName("Attributes");
		for(int i = 0; i<cn.getLength(); i++){
			if(cn.item(i).getAttributes().getNamedItem("Category").getTextContent().equals(core.CTIConstants.RESOURCE_CATEGORY)){
				attributes = cn.item(i).getChildNodes();
			}
		}
		
		if(attributes != null){
			for(int i = 0; i<attributes.getLength(); i++){
				if(attributes.item(i).hasAttributes()){
					String att_id = attributes.item(i).getAttributes().getNamedItem("AttributeId").getTextContent();
						
					if(att_id.equals("full-path")){
						NodeList av = attributes.item(i).getChildNodes();
						for(int j = 0; j<av.getLength(); j++){
							if(av.item(j).getNodeName().equals("AttributeValue")){
								resourceAttributeValue = av.item(j).getTextContent();
							}
						}
						
					}
				}
			}
		}
		
		Logger.debug("Checking whether " + resourceAttributeValue + " contains file://");
		if(objectXACMLAuthz.getResponse().getResult().getDecision().getDecision().toString().equals("Permit") && resourceAttributeValue.contains("file://")){
			Logger.debug("Found an encrypted token AND a permit decision. Attempting to decrypt");
			assertion.getAttributeStatements().add(decrypt_token(objectXACMLAuthz));
		}else{
			Logger.debug("Resource did not contain 'file://' or decision was not permit");
		}
		
		assertion.getStatements().add(objectXACMLAuthz);
		ResponseGenerator responseGenerator = new ResponseGenerator();
		Response response = responseGenerator.makeResponse(assertion);
		
		SignatureHelper helper = new SignatureHelper();

		Signature sig = helper.makeSignature(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1, helper.createCredential());
		response.setSignature(sig);

		IssuerBuilder issuerBuilder = (IssuerBuilder) builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
		Issuer issuer = issuerBuilder.buildObject();
		issuer.setValue(Config.Saml.response_issuers.get(0));
		
		response.setIssuer(issuer);

		List<Signature> signatureList = new ArrayList<Signature>();
		signatureList.add(sig);

		MarshallerFactory marshallerFactory = org.opensaml.xml.Configuration.getMarshallerFactory();
		Marshaller marshaller = marshallerFactory.getMarshaller(Response.DEFAULT_ELEMENT_NAME);

		String wrappedResponse = null;
		try {
			marshaller.marshall(response);
			org.apache.xml.security.Init.init();
			Signer.signObjects(signatureList);
			wrappedResponse = responseGenerator.marshallToString(response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wrappedResponse;
	}
	
	private RequestType makeRequestFromString(String requestString) {
		requestString = requestString.substring(requestString.indexOf("<Request"), requestString.indexOf("/Request>") + 9); // extract just the <request>
		requestString = requestString.replace(CTIConstants.XACML3_XMLNS, CTIConstants.XACML2_XMLNS); // hack to work w/ opensaml
		requestString = requestString.replaceAll("\n", "").replaceAll("\r", ""); // get rid of new lines

		// Create input stream from modifiedResponseString
		InputStream inStr = new ByteArrayInputStream(requestString.getBytes(StandardCharsets.UTF_8));

		// Create BasicParserPool(Sun code)
		BasicParserPool pool = new BasicParserPool();
		pool.setNamespaceAware(true);

		// Create Document object by parsing the request file
		// This Document class is from org.w3c.dom.Document which represents an
		// HTML or XML document and represents the root of the document tree
		Document doc = null;
		try {
			doc = pool.parse(inStr);
		} catch (XMLParserException e) {
			e.printStackTrace();
		}

		// Get the root of the Document object
		Element requestRoot = doc.getDocumentElement();

		// Get unmarshaller factory from which the unmarshaller for a
		// RequestType is retrieved (openSAML)
		UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
		Unmarshaller requestUnmarshaller = unmarshallerFactory.getUnmarshaller(requestRoot);

		// Create ResponseType object by unmarshalling the file
		RequestType request = null;
		try {
			request = (RequestType) requestUnmarshaller.unmarshall(requestRoot);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return request;
	}
	
	private ResponseType makeResponseFromString(String unmod_request) {
		String responseString = unmod_request.replaceAll("\n", "").replaceAll("\r", "");
		responseString = responseString.replace(CTIConstants.XACML3_XMLNS, CTIConstants.XACML2_XMLNS);
		
		// Create input stream from modifiedResponseString
		InputStream inStr = new ByteArrayInputStream(responseString.getBytes(StandardCharsets.UTF_8));

		// Create BasicParserPool(Sun code)
		BasicParserPool pool = new BasicParserPool();
		pool.setNamespaceAware(true);

		// Create Document object by parsing the request file
		// This Document class is from org.w3c.dom.Document which represents an
		// HTML or XML document and represents the root of the document tree
		Document doc = null;
		try {
			doc = pool.parse(inStr);
		} catch (XMLParserException e) {
			e.printStackTrace();
		}

		// Get the root of the Document object
		Element resRoot = doc.getDocumentElement();

		// Get unmarshaller factory from which the unmarshaller for a
		// RequestType is retrieved (openSAML)
		UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
		Unmarshaller requestUnmarshaller = unmarshallerFactory.getUnmarshaller(resRoot);

		// Create ResponseType object by unmarshalling the file
		ResponseType response = null;
		try {
			response = (ResponseType) requestUnmarshaller.unmarshall(resRoot);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
	/**
	 * Token extracter is used to essentially parse an incoming XACML request from a
	 * Tahoe PEP in search of an encrypted token, which will need to be decrypted if
	 * a Permit decision is made. This method is called when a permit decision is
	 * made for a request from a Tahoe PEP. It searches for any included token, then
	 * decrypts it, then re-inserts it into the SAML wrapping the XACML.
	 * 
	 * @param objectXACMLAuthz
	 * @return a SAML attribute statement
	 * @throws IOException
	 */
	//TODO this probably needs to be redone for XACML3
	static AttributeStatement decrypt_token(XACMLAuthzDecisionStatementType objectXACMLAuthz) {
		XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();

		String resourceAttributeValue = "";
		NodeList attributes = null;
		NodeList cn = objectXACMLAuthz.getResponse().getResult().getDOM().getElementsByTagName("Attributes");
		for(int i = 0; i<cn.getLength(); i++){
			if(cn.item(i).getAttributes().getNamedItem("Category").getTextContent().equals(core.CTIConstants.RESOURCE_CATEGORY)){
				attributes = cn.item(i).getChildNodes();
			}
		}
		
		if(attributes != null){
			for(int i = 0; i<attributes.getLength(); i++){
				if(attributes.item(i).hasAttributes()){
					String att_id = attributes.item(i).getAttributes().getNamedItem("AttributeId").getTextContent();
						
					if(att_id.equals("token")){
						NodeList av = attributes.item(i).getChildNodes();
						for(int j = 0; j<av.getLength(); j++){
							if(av.item(j).getNodeName().equals("AttributeValue")){
								resourceAttributeValue = av.item(j).getTextContent();
							}
						}
						
					}
				}
			}
		}

		Logger.debug("Just extracted the encrypted token from the request: " + resourceAttributeValue);

		resourceAttributeValue = core.Utils.decodeBase64(resourceAttributeValue);
		
		String tmp = String.join("\n", resourceAttributeValue);
		String decryptedToken = null;
		try {
			decryptedToken = PGPHelper.DecryptString(tmp, Config.Pgp.token_private_key,
					Config.Pgp.token_private_key_pw);
		} catch (Exception e) {
			Logger.error(e);
		}
		
		Logger.debug("Just decrypted the token: " + decryptedToken);

		SAMLObjectBuilder<?> attrBuilder = (SAMLObjectBuilder<?>) builderFactory
				.getBuilder(Attribute.DEFAULT_ELEMENT_NAME);
		Attribute attrGroups = (Attribute) attrBuilder.buildObject();
		attrGroups.setName("Groups");

		XMLObjectBuilder<?> stringBuilder = builderFactory.getBuilder(XSString.TYPE_NAME);
		XSString attrNewValue = (XSString) stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME,
				XSString.TYPE_NAME);
		try {
			attrNewValue.setValue(core.Utils.encodeBase64(decryptedToken));
		} catch (Exception e) {
			Logger.error(e);
			attrNewValue.setValue("");
		}
		attrGroups.getAttributeValues().add(attrNewValue);

		SAMLObjectBuilder<?> attrStatementBuilder = (SAMLObjectBuilder<?>) builderFactory
				.getBuilder(AttributeStatement.DEFAULT_ELEMENT_NAME);
		AttributeStatement attrStatement = (AttributeStatement) attrStatementBuilder.buildObject();
		attrStatement.getAttributes().add(attrGroups);

		return attrStatement;
	}
	
}

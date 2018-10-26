package helpers;

import java.io.IOException;

import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.impl.ResponseBuilder;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.util.XMLHelper;
import org.pmw.tinylog.Logger;
import org.w3c.dom.Element;

/**
 * This is a helper class to create a SAML Response.
 * @author 
 * Justin Fleming - fleminjr@critical.com
 * Jeremy Fields - fieldsjd@critical.com
 */
public class ResponseGenerator {
	/**
	 * Instantiate a global XMLObjectBuilderFactory and initialize to null
	 */
	private XMLObjectBuilderFactory builderFactory = null;
	
	/**
	 * Default constructor. Calls the bootstrap method,
	 * and instantiates the builderFactory object
	 * @throws IOException 
	 */
	public ResponseGenerator(){
		try{
			DefaultBootstrap.bootstrap();
		} catch (Exception e) {
			Logger.error(e);
		}
		builderFactory = Configuration.getBuilderFactory();
	}
	
	/**
	 * This method takes an Assertion object, creates a Response object from it
	 * 
	 * @param assertion Assertion object used to construct the Response object
	 * @return Response
	 */
	public Response makeResponse(Assertion assertion){
		ResponseBuilder responseBuilder = (ResponseBuilder) builderFactory.getBuilder(Response.DEFAULT_ELEMENT_NAME);
		
		Response response = responseBuilder.buildObject();
		
		response.getAssertions().add(assertion);
		response.setIssueInstant(new DateTime());
		
		//do something about the status/status code here!!!!!!
		
		return response;
	}
	
	/**
	 * This function marshalls a Response to a String
	 * 
	 * @param response Response object used to marshall
	 * @return String representing the marshalled Response object
	 * @throws IOException 
	 */
	public String marshallToString(Response response) {
		// Get marshaller factory
		MarshallerFactory marshallerFactory = Configuration
				.getMarshallerFactory();

		// Get response marshaller and marshall it
		Marshaller marshaller = marshallerFactory
				.getMarshaller(response);
		Element responseElement = null;
		String xmlString = "";
		try {
			responseElement = marshaller.marshall(response);
			xmlString =  XMLHelper.nodeToString(responseElement);
		} catch (Exception e) {
			Logger.error(e);
		}		
		return xmlString;
	}
}

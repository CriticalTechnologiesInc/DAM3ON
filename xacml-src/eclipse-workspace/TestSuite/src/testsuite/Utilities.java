package testsuite;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

public class Utilities {
	
	public static void main(String[] args) throws MalformedURLException{
		checkSchema();
	}

	
	
	/**
	 * Gets a path to a valid file from a user, and returns that as a String.
	 * 
	 * @return String of a valid path to a file
	 */
	public static String getConfigPathFromUser(){
		Scanner scan = new Scanner(System.in);
		boolean validPath = false;
		String confPath = "";

		// make sure user gives valid path
		while (!validPath) {
			System.out.print("Full path to configuration file?: ");

			confPath = scan.next();

			if (core.Utils.validFilePath(confPath)) {
				validPath = true;
			} else {
				System.out.println("Invalid path! Try again.\n\n");
				while(scan.hasNext())
					scan.nextLine(); // clear buffer
			}
		}
		scan.close();
		
		return confPath;
	}
	
	public static void checkSchema() throws MalformedURLException{
		System.out.println("Starting schema validation");
		URL schemaFile1 = new URL("https://docs.oasis-open.org/security/saml/v2.0/saml-schema-assertion-2.0.xsd");
		URL schemaFile2 = new URL("https://ctidev4.critical.com/certs/cticertschema.xsd");
		String xmlFile = "https://ctidev4.critical.com/certs/guard.xml";
		Source xmlStream = new StreamSource(xmlFile);
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			Schema schema = schemaFactory.newSchema(schemaFile1);
			Validator validator = schema.newValidator();
			validator.validate(xmlStream);
			System.out.println("Passed " + schemaFile1);
		} catch (SAXException e) {
			System.out.println(xmlStream.getSystemId() + " failed " + schemaFile1 + ":\n" + e);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			Schema schema = schemaFactory.newSchema(schemaFile2);
			Validator validator = schema.newValidator();
			validator.validate(xmlStream);
			System.out.println("Passed " + schemaFile2);
		} catch (SAXException e) {
			System.out.println(xmlStream.getSystemId() + " failed " + schemaFile2 + ":\n" + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done.");
	}
	
}

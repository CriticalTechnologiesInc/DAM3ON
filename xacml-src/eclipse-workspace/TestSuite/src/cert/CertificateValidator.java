package cert;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.XMLParserException;
import org.xml.sax.SAXException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import core.Config;
import validator.BadDigestException;
import validator.CTICertificate;

public class CertificateValidator {
	@Parameter(names = { "-cert", "--certificate" }, description = "REQUIRED: Location of certificate file")
	private static String cert;

	@Parameter(names = { "-conf", "--configuration" }, description = "Location of config file. Required for -all and -sig")
	private static String conf;

	@Parameter(names = { "-all" }, description = "Run all tests and show additional details about certificate")
	private static boolean all = false;

	@Parameter(names = { "-sig", "--signature" }, description = "Validate Signature")
	private static boolean sig = false;

	@Parameter(names = { "-sch", "--schema" }, description = "Validate Schema")
	private static boolean schema = false;
	
	@Parameter(names = {"-h", "--help" }, description = "Prints usage instructions for this program")
	private static boolean help = false;

	public static void main(String[] args) {
		CertificateValidator cv = new CertificateValidator();
		JCommander jcm = null;
		try{
			jcm = new JCommander(cv, args);
		}catch(Exception e){
			System.out.println(e);
			System.exit(1);
		}
		
		if(help){
			jcm.usage();
			System.exit(2);
		}
		
		if(cert == null){
			System.out.println("-cert flag is required. Use '-h' for help");
			System.exit(1);
		}
		
		if (!core.Utils.validFilePath(cert)) {
			System.out.println("Invalid cert path!");
			jcm.usage();
			System.exit(1);
		}
	
		if (all || sig) {

			if (core.Utils.validFilePath(conf)) {
				new Config(new File(conf).getAbsolutePath());
			} else {
				System.out.println("Config path was not given or it was invalid! Valid configuration file is required for this option");
				jcm.usage();
				System.exit(1);
			}
		}

	
		if (all) {
			allDetails();
			schemas();
		} else if(sig || schema){
			if (sig)
				validateSignature();
			if (schema)
				schemas();
		}else{
			jcm.usage();
		}
		
	}

	public static void allDetails() {
		CTICertificate certObj;
		try {
			certObj = new CTICertificate(new File(cert));
		} catch (NullPointerException | ParseException | IOException | XMLParserException | UnmarshallingException | ConfigurationException | BadDigestException | NoSuchAlgorithmException e) {
			certObj = null;
			e.printStackTrace();
		}

		System.out.println("This certificate is issued by: " + certObj.getIssuerName());
		System.out.println("Effective on: " + certObj.getValidityNotBefore());
		System.out.println("Expires on: " + certObj.getValidityNotAfter());
		System.out.println("Subject: " + certObj.getSubjectName());
		System.out.println("Signatured validated: " + certObj.getValidSig());
		System.out.println("Digest validated: " + certObj.getValidDigest());
		System.out.println("Valid date: " + certObj.getValidDate());
		System.out.println("Valid property list: " + certObj.getValidProps());
	}

	public static void validateSignature() {
		CTICertificate certObj;
		try {
			certObj = new CTICertificate(new File(cert));
		} catch (NullPointerException | ParseException | IOException | XMLParserException | UnmarshallingException | ConfigurationException | BadDigestException | NoSuchAlgorithmException e) {
			certObj = null;
			// probably not stack trace here...but i don't know what
			e.printStackTrace();
		}
		System.out.println("Digest validated: " + certObj.getValidDigest());
		System.out.println("Signatured validated: " + certObj.getValidSig());
	}

	public static void schemas() {
		URL schemaFile1 = null;
		URL schemaFile2 = null;

		try {
			schemaFile1 = new URL("https://docs.oasis-open.org/security/saml/v2.0/saml-schema-assertion-2.0.xsd");
			schemaFile2 = new URL("https://ctidev4.critical.com/certs/cticertschema.xsd");
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			System.exit(2);
		}

		checkSchema(schemaFile1, new File(cert));
		checkSchema(schemaFile2, new File(cert));
	}

	public static boolean checkSchema(URL schemaUrl, File certificate) {
		System.out.println("Starting schema validation of: " + schemaUrl.toString() + "  - (this can take a minute)");
		Source xmlStream = new StreamSource(certificate);
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			Schema schema = schemaFactory.newSchema(schemaUrl);
			Validator validator = schema.newValidator();
			validator.validate(xmlStream);
			System.out.println("Passed.");
			return true;
		} catch (SAXException e) {
			System.out.println(xmlStream.getSystemId() + " failed " + schemaUrl.toString() + ":\n" + e);
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}
}

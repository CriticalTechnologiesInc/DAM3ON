package validator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.pmw.tinylog.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import core.Utils;

import java.util.ArrayList;
import java.util.Date;

import xacml.SignatureHelper;
import xacml.PropTypes;
import xacml.Property;

public class CTICertificate {

	private boolean isBootStrapped = false;
	
	protected String IssuerName;
	protected Date ValidityNotBefore;
	protected Date ValidityNotAfter;
	protected String SubjectName;
	protected String SubjectDescription;
	protected String SubjectHashInfoAlgo;
	protected String SubjectHashInfoHash;
	protected int SubjectLength;
	protected String SubjectVersion;
	protected String id;
	protected String hashOfCert;
	protected ArrayList<Property> SubjectProperties = new ArrayList<Property>();
	protected String fullCertificate;
	protected boolean validDate = false;
	protected boolean validProps = false;
	protected boolean validSig = false;
	protected boolean validDigest = false;
	
	public static void main(String[] args) throws FileNotFoundException, NullPointerException, ParseException {
		String url = "https://ctidev4.critical.com/certs/guard.xml";
		CTICertificate sable;
		
		try{
//			sable = new CTICertificate(new File("c:\\users\\advan\\desktop\\withid.xml"));
			sable = new CTICertificate(new URL(url));
		}catch (Exception e){
			e.printStackTrace();
			sable = null;
		}

		
		if(sable != null){
			System.out.println("This certificate is issued by: " + sable.getIssuerName());
			System.out.println("Effective on: " + sable.getValidityNotBefore());
			System.out.println("Expires on: " + sable.getValidityNotAfter());
			System.out.println("Subject: " + sable.getSubjectName());
			System.out.println("Signatured validated: " + sable.getValidSig());
			System.out.println("Digest validated: " + sable.getValidDigest());
			System.out.println("Cert hash: " + sable.getHashOfCert());
			for(Property p : sable.getSubjectProperties()){
				System.out.println("property: " + p.propertyName + " with value: " + p.propertyValue);
			}
		}else{
			System.out.println("There is some problem with your certificate!");
		}


	}
	public CTICertificate(){}
	
	public CTICertificate(String cert) throws ParseException, NullPointerException, IOException, XMLParserException, UnmarshallingException, ConfigurationException, BadDigestException, NoSuchAlgorithmException  {
        parseCertificate(cert);
		validate();
	}
	
	public CTICertificate(File fd) throws ParseException, NullPointerException, IOException, XMLParserException, UnmarshallingException, ConfigurationException, BadDigestException, NoSuchAlgorithmException  {
        parseCertificate(Utils.readFile(fd));
		validate();
	}
	
	public CTICertificate(URL website) throws ParseException, NullPointerException, IOException, XMLParserException, UnmarshallingException, ConfigurationException, BadDigestException, NoSuchAlgorithmException  {
        parseCertificate(Utils.getTextFromUrl(website));
		validate();
	}

	
	private void parseCertificate(String cert)throws ConfigurationException, BadDigestException, NoSuchAlgorithmException, XMLParserException, UnmarshallingException, ParseException {
		fullCertificate = cert;
		validDigest = new validateDigest().validDigest(fullCertificate);
		if(!validDigest)
			throw new BadDigestException("Bad digest!");
			
		
		setHashOfCert(Utils.byteArrayToHex(MessageDigest.getInstance("SHA-256").digest(cert.getBytes(StandardCharsets.UTF_8))));
		
	     // Initialize the library
        DefaultBootstrap.bootstrap(); 
         
        // Get parser pool manager
        BasicParserPool ppMgr = new BasicParserPool();
        ppMgr.setNamespaceAware(true);
         
        // Parse metadata file
        Document inCommonMDDoc = ppMgr.parse(new ByteArrayInputStream(fullCertificate.getBytes(StandardCharsets.UTF_8)));
        Element metadataRoot = inCommonMDDoc.getDocumentElement();
         
        // Get apropriate unmarshaller
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(metadataRoot);
        
        Assertion ass = (Assertion) unmarshaller.unmarshall(metadataRoot);
        setId(ass.getID());
        
        setIssuerName(ass.getIssuer().getValue());
        setValidityNotBefore(ass.getConditions().getNotBefore().toDate());
        setValidityNotOnOrAfter(ass.getConditions().getNotOnOrAfter().toDate());
        
        setSubjectHashInfoHash(ass.getSubject().getNameID().getValue());
        setSubjectHashInfoAlgo(ass.getSubject().getNameID().getNameQualifier());

        
        AttributeStatement as = ass.getAttributeStatements().get(0);
        for(Attribute att : as.getAttributes()){
        	if(att.getName().equals("cert-info")){
        		XMLObject attVal = att.getAttributeValues().get(0);
        		for(XMLObject xmo : attVal.getOrderedChildren()){
        			if(xmo.getDOM().getNodeName().toLowerCase().equals("name")){
        				setSubjectName(xmo.getDOM().getTextContent());
        			}else if(xmo.getDOM().getNodeName().toLowerCase().equals("description")){
        				setSubjectDescription(xmo.getDOM().getTextContent());
        			}else if (xmo.getDOM().getNodeName().toLowerCase().equals("length")){
        				int length = Integer.parseInt(xmo.getDOM().getTextContent());
        				setSubjectLength(length);
        			}else if(xmo.getDOM().getNodeName().toLowerCase().equals("version")){
        				setSubjectVersion(xmo.getDOM().getTextContent());
        			}else if(xmo.getDOM().getNodeName().toLowerCase().equals("property_info")){
        				
        				for(XMLObject entry : xmo.getOrderedChildren()){
        					Property p = new Property();
        					
        					for(XMLObject elem : entry.getOrderedChildren()){
	        					if(elem.getDOM().getNodeName().toLowerCase().equals("property")){
	        						p.propertyName = elem.getDOM().getTextContent();
	        					}else if(elem.getDOM().getNodeName().toLowerCase().equals("proof")){
	        						p.propertyValue = elem.getDOM().getTextContent();
	        						p.type = PropTypes.PROOF;
	        					}else if(elem.getDOM().getNodeName().toLowerCase().equals("reference")){
	        						p.propertyValue = elem.getDOM().getTextContent();
	        						p.type = PropTypes.REF;
	        					}else if(elem.getDOM().getNodeName().toLowerCase().equals("insured")){
	        						p.propertyValue = elem.getDOM().getTextContent();
	        						p.type = PropTypes.INSURED;
	        					}else if(elem.getDOM().getNodeName().toLowerCase().equals("pentested")){
	        						p.propertyValue = elem.getDOM().getTextContent();
	        						p.type = PropTypes.PENTEST;
	        					}
	        					
	        					if(p.type == null){
	        						p.type = PropTypes.FUCKYOU;
	        					}
        					}
        					setSubjectProperty(p);
        				}
        			}
        		}
        	}
        }
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (!CTICertificate.class.isAssignableFrom(obj.getClass())) {
	        return false;
	    }
	    final CTICertificate other = (CTICertificate) obj;
	    if ((this.hashOfCert == null) ? (other.hashOfCert != null) : !this.hashOfCert.equals(other.hashOfCert)) {
	        return false;
	    }
	    return true;
	}
	
	private void validate(){
		// Date validation
		Date d = new Date();
		if(!(d.before(ValidityNotBefore) || d.after(ValidityNotAfter))){
			validDate = true;
		}
		
		// Signature validation
		if(fullCertificate != null){
			validSig = new SignatureHelper().verifyAssertionSignature(fullCertificate);
		}
		
		if(!SubjectProperties.isEmpty()){
			validProps = true;
		}
		
	}
	
	public String getIssuerName() {return IssuerName;}
	public Date getValidityNotBefore() {return ValidityNotBefore;}
	public Date getValidityNotAfter() {	return ValidityNotAfter;}
	
	public String getValidityNotBeforeStr() {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss"); 
		return df.format(getValidityNotBefore());
	}
	public String getValidityNotAfterStr() {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
		return df.format(getValidityNotAfter());
	}
	
	public String getSubjectName() {return SubjectName;}
	public String getSubjectDescription() {return SubjectDescription;}
	public String getSubjectHashInfoAlgo() {return SubjectHashInfoAlgo;}
	public String getSubjectHashInfoHash() {return SubjectHashInfoHash;}
	public int getSubjectLength() {return SubjectLength;}
	public String getSubjectVersion() {return SubjectVersion;}
	public String getId() {return id;}
	public String getHashOfCert() {return hashOfCert;}
	public String getFullCertificate() {return fullCertificate;}
	public ArrayList<Property> getSubjectProperties() {return SubjectProperties;}
	
	public boolean getValidDate(){return validDate;}
	public boolean getValidProps(){return validProps;}
	public boolean getValidDigest(){return validDigest;}
	public boolean getValidSig(){return validSig;}
	

	protected void setHashOfCert(String hash){
		hashOfCert = hash;
	}
	
	protected void setId(String Id){
		id = Id;
	}
	
	protected void setIssuerName(String issuerName) {
		IssuerName = issuerName;
	}

	protected void setValidityNotBefore(Date validityNotBefore) throws ParseException {
		ValidityNotBefore = validityNotBefore;
	}

	protected void setValidityNotOnOrAfter(Date validityNotAfter) throws ParseException {
		ValidityNotAfter = validityNotAfter;
	}

	protected void setSubjectName(String subjectName) {
		SubjectName = subjectName;
	}

	protected void setSubjectDescription(String subjectDescription) {
		SubjectDescription = subjectDescription;
	}

	protected void setSubjectHashInfoAlgo(String subjectHashInfoAlgo) {
		SubjectHashInfoAlgo = subjectHashInfoAlgo;
	}

	protected void setSubjectHashInfoHash(String subjectHashInfoHash) {
		SubjectHashInfoHash = subjectHashInfoHash;
	}

	protected void setSubjectLength(int length){
		SubjectLength = length;
	}
	
	protected void setSubjectVersion(String version){
		SubjectVersion = version;
	}
	
	protected void setSubjectProperty(Property p) {
		SubjectProperties.add(p);	
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
	
}

package validator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import core.Utils;
import java.util.ArrayList;
import java.util.Date;

import xacml.SignatureHelper;
import xacml.PropTypes;
import xacml.Property;

public class InsuranceCertificate extends CTICertificate{
	private String companyName;
	private String agentName;
	private String address;
	private String phone;
	private String email;
	private ArrayList<String> policyConditions;
	private int amountInsured;
	private String fullPolicy;
	
	// main() is only for demonstration & testing
	public static void main(String[] args) throws FileNotFoundException, NullPointerException, ParseException {
		String url = "https://ctidev4.critical.com/certs/sampleInsuranceCert.xml";
		InsuranceCertificate sable;
		
		try{
			sable = new InsuranceCertificate(new URL(url));
		}catch (Exception e){
			e.printStackTrace();
			sable = null;
		}

		
		if(sable != null){
			System.out.println("This certificate is issued by: " + sable.getIssuerName());
			System.out.println("Effective on: " + sable.getValidityNotBefore());
			System.out.println("Expires on: " + sable.getValidityNotAfter());
			System.out.println("Signatured validated: " + sable.getValidSig());
			System.out.println("Digest validated: " + sable.getValidDigest());
			System.out.println("Cert hash: " + sable.getHashOfCert());
			System.out.println("Properties: " + sable.getSubjectProperties());
			System.out.println("Company name: " + sable.getCompanyName());
			System.out.println("Agent name: " + sable.getAgentName());
			System.out.println("Address: " + sable.getAddress());
			System.out.println("Phone: " + sable.getPhone());
			System.out.println("Email: " + sable.getEmail());
			System.out.println("Amount Insured: " + sable.getAmountInsured());
			System.out.println("Full Policy: " + sable.getFullPolicy());
			System.out.println("Conditions: " + sable.getPolicyConditions());
			
			
			

		}else{
			System.out.println("There is some problem with your certificate!");
		}


	}
	
	public InsuranceCertificate(String cert) throws ParseException, NullPointerException, IOException, XMLParserException, UnmarshallingException, ConfigurationException, BadDigestException, NoSuchAlgorithmException  {
        parseCertificate(cert);
		validate();
	}
	
	public InsuranceCertificate(File fd) throws ParseException, NullPointerException, IOException, XMLParserException, UnmarshallingException, ConfigurationException, BadDigestException, NoSuchAlgorithmException  {
        parseCertificate(Utils.readFile(fd));
		validate();
	}
	
	public InsuranceCertificate(URL website) throws ParseException, NullPointerException, IOException, XMLParserException, UnmarshallingException, ConfigurationException, BadDigestException, NoSuchAlgorithmException  {
		parseCertificate(Utils.getTextFromUrl(website));
		validate();
	}

	private void parseCertificate(String cert) throws ConfigurationException, XMLParserException, UnmarshallingException, ParseException, BadDigestException, NoSuchAlgorithmException{
		policyConditions = new ArrayList<String>();
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
        	if(att.getName().equals("insurance-info")){
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
        			}else if(xmo.getDOM().getNodeName().toLowerCase().equals("policy")){
        				for(XMLObject xmo2 : xmo.getOrderedChildren()){
	        				if(xmo2.getDOM().getNodeName().toLowerCase().equals("properties")){
		        				for(XMLObject entry : xmo2.getOrderedChildren()){
		        					Property p = new Property();
		    						p.type = PropTypes.PROOF;
		        					
		        					for(XMLObject elem : entry.getOrderedChildren()){
			        					if(elem.getDOM().getNodeName().toLowerCase().equals("name")){
			        						p.propertyName = elem.getDOM().getTextContent();
			        					}else if(elem.getDOM().getNodeName().toLowerCase().equals("description")){
			        						p.propertyValue = elem.getDOM().getTextContent();
			        					}
		        					}
		        					setSubjectProperty(p);
		        				}
	        				}else if(xmo2.getDOM().getNodeName().toLowerCase().equals("conditions")){
	        					for(XMLObject cond : xmo2.getOrderedChildren()){
	        						setPolicyConditions(cond.getDOM().getTextContent());
	        					}
	        				}else if(xmo2.getDOM().getNodeName().toLowerCase().equals("amount_insured")){
	        					setAmountInsured(Integer.parseInt(xmo2.getDOM().getTextContent()));
	        				}else if(xmo2.getDOM().getNodeName().toLowerCase().equals("full_policy")){
	        					setFullPolicy(xmo2.getDOM().getTextContent());
	        				}
        				}
        			}else if(xmo.getDOM().getNodeName().toLowerCase().equals("provider")){
        				for(XMLObject c_elem : xmo.getOrderedChildren()){
        					if(c_elem.getDOM().getNodeName().toLowerCase().equals("company_name")){
        						setCompanyName(c_elem.getDOM().getTextContent());
        					}else if(c_elem.getDOM().getNodeName().toLowerCase().equals("agent_name")){
        						setAgentName(c_elem.getDOM().getTextContent());
        					}else if(c_elem.getDOM().getNodeName().toLowerCase().equals("address")){
        						setAddress(c_elem.getDOM().getTextContent());
        					}else if(c_elem.getDOM().getNodeName().toLowerCase().equals("phone")){
        						setPhone(c_elem.getDOM().getTextContent());
        					}else if(c_elem.getDOM().getNodeName().toLowerCase().equals("email")){
        						setEmail(c_elem.getDOM().getTextContent());
        					}
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
	    if (!InsuranceCertificate.class.isAssignableFrom(obj.getClass())) {
	        return false;
	    }
	    final InsuranceCertificate other = (InsuranceCertificate) obj;
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
	
	public String getCompanyName() {return companyName;}
	public String getAgentName() {return agentName;}
	public String getAddress() {return address;}
	public String getPhone() {return phone;}
	public String getEmail() {return email;}

	public ArrayList<String> getPolicyConditions() {return policyConditions;}
	public int getAmountInsured() {return amountInsured;}
	public String getFullPolicy() {return fullPolicy;}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public void setPolicyConditions(String newCond) {
		this.policyConditions.add(newCond);
	}
	
	public void setAmountInsured(int amount) {
		this.amountInsured = amount;
	}
	
	public void setFullPolicy(String fullPolicy) {
		this.fullPolicy = fullPolicy;
	}

}

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
import java.util.ArrayList;
import java.util.Date;

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
import xacml.SignatureHelper;
import xacml.PropTypes;
import xacml.Property;

public class ProofCertificate extends CTICertificate {
	private String file;
	private String checkerName;
	private String checkerVersion;
	private ArrayList<String> checkerArgs;
	private ArrayList<String> dependencies;

	public static void main(String[] args) throws FileNotFoundException, NullPointerException, ParseException {
		String url = "https://ctidev4.critical.com/certs/sampleProofCert.xml";
		ProofCertificate sable;

		try {
			// sable = new CTICertificate(new File("c:\\users\\advan\\desktop\\withid.xml"));
			sable = new ProofCertificate(new URL(url));
		} catch (Exception e) {
			e.printStackTrace();
			sable = null;
		}

		if (sable != null) {
			System.out.println("This certificate is issued by: " + sable.getIssuerName());
			System.out.println("Effective on: " + sable.getValidityNotBefore());
			System.out.println("Expires on: " + sable.getValidityNotAfter());
			System.out.println("Signatured validated: " + sable.getValidSig());
			System.out.println("Digest validated: " + sable.getValidDigest());
			System.out.println("Cert hash: " + sable.getHashOfCert());
			System.out.println("File: " + sable.getFile());
			System.out.println("Properties: " + sable.getSubjectProperties());
			System.out.println("Checker Name: " + sable.getCheckerName());
			System.out.println("Checker Version: " + sable.getCheckerVersion());
			System.out.println("Checker Args: " + sable.getCheckerArgs());
			System.out.println("Dependencies: " + sable.getDependencies());
		} else {
			System.out.println("There is some problem with your certificate!");
		}

	}

	public ProofCertificate(String cert) throws ConfigurationException, BadDigestException, NoSuchAlgorithmException, XMLParserException, UnmarshallingException, ParseException,
			NullPointerException, IOException {
		parseCertificate(cert);
		validate();
	}

	public ProofCertificate(File fd) throws ConfigurationException, BadDigestException, NoSuchAlgorithmException, XMLParserException, UnmarshallingException, ParseException,
			NullPointerException, IOException {
		parseCertificate(Utils.readFile(fd));
		validate();
	}

	public ProofCertificate(URL website)
			throws IOException, ConfigurationException, BadDigestException, NoSuchAlgorithmException, XMLParserException, UnmarshallingException, ParseException {
		parseCertificate(Utils.getTextFromUrl(website));
		validate();
	}

	private void parseCertificate(String cert)
			throws ConfigurationException, BadDigestException, NoSuchAlgorithmException, XMLParserException, UnmarshallingException, ParseException {
		System.out.println("Running TestProofCertificate.parse()");
		checkerArgs = new ArrayList<String>();
		dependencies = new ArrayList<String>();
		fullCertificate = cert;

		System.out.println("__________________ fullCertificate:\n" + fullCertificate);

		validDigest = new validateDigest().validDigest(fullCertificate);
		if (!validDigest)
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
		for (Attribute att : as.getAttributes()) {
			if (att.getName().equals("proof-info")) {
				XMLObject attVal = att.getAttributeValues().get(0);
				for (XMLObject xmo : attVal.getOrderedChildren()) {
					if (xmo.getDOM().getNodeName().toLowerCase().equals("name")) {
						setSubjectName(xmo.getDOM().getTextContent());
					} else if (xmo.getDOM().getNodeName().toLowerCase().equals("description")) {
						setSubjectDescription(xmo.getDOM().getTextContent());
					} else if (xmo.getDOM().getNodeName().toLowerCase().equals("length")) {
						int length = Integer.parseInt(xmo.getDOM().getTextContent());
						setSubjectLength(length);
					} else if (xmo.getDOM().getNodeName().toLowerCase().equals("version")) {
						setSubjectVersion(xmo.getDOM().getTextContent());
					} else if (xmo.getDOM().getNodeName().toLowerCase().equals("file")) {
						setFile(xmo.getDOM().getTextContent());
					} else if (xmo.getDOM().getNodeName().toLowerCase().equals("properties")) {

						for (XMLObject entry : xmo.getOrderedChildren()) {
							Property p = new Property();
							p.type = PropTypes.PROOF;

							for (XMLObject elem : entry.getOrderedChildren()) {
								if (elem.getDOM().getNodeName().toLowerCase().equals("name")) {
									p.propertyName = elem.getDOM().getTextContent();
								} else if (elem.getDOM().getNodeName().toLowerCase().equals("description")) {
									p.propertyValue = elem.getDOM().getTextContent();
								}
							}
							setSubjectProperty(p);
						}
					} else if (xmo.getDOM().getNodeName().toLowerCase().equals("checker")) {
						for (XMLObject c_elem : xmo.getOrderedChildren()) {
							if (c_elem.getDOM().getNodeName().toLowerCase().equals("name")) {
								setCheckerName(c_elem.getDOM().getTextContent());
							} else if (c_elem.getDOM().getNodeName().toLowerCase().equals("version")) {
								setCheckerVersion(c_elem.getDOM().getTextContent());
							} else if (c_elem.getDOM().getNodeName().toLowerCase().equals("args")) {
								for (XMLObject arg : c_elem.getOrderedChildren()) {
									if (arg.getDOM().getNodeName().toLowerCase().equals("arg")) {
										setCheckerArg(arg.getDOM().getTextContent());
									}
								}
							}
						}
					} else if (xmo.getDOM().getNodeName().toLowerCase().equals("dependencies")) {
						for (XMLObject dep : xmo.getOrderedChildren()) {
							if (dep.getDOM().getNodeName().toLowerCase().equals("dependency")) {
								setDependency(dep.getDOM().getTextContent());
							}
						}
					}
				}
			}
		}
	}

	public ArrayList<String> getCheckerArgs() {
		return checkerArgs;
	}

	public String getCheckerVersion() {
		return checkerVersion;
	}

	public String getCheckerName() {
		return checkerName;
	}

	public ArrayList<String> getDependencies() {
		return dependencies;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public void setCheckerName(String checkerName) {
		this.checkerName = checkerName;
	}

	public void setCheckerVersion(String checkerVersion) {
		this.checkerVersion = checkerVersion;
	}

	public void setCheckerArg(String newArg) {
		this.checkerArgs.add(newArg);
	}

	public void setDependency(String newDep) {
		this.dependencies.add(newDep);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!ProofCertificate.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final ProofCertificate other = (ProofCertificate) obj;
		if ((this.hashOfCert == null) ? (other.hashOfCert != null) : !this.hashOfCert.equals(other.hashOfCert)) {
			return false;
		}
		return true;
	}

	protected void validate() {
		// Date validation
		Date d = new Date();
		if (!(d.before(ValidityNotBefore) || d.after(ValidityNotAfter))) {
			validDate = true;
		}

		// Signature validation
		if (fullCertificate != null) {
			validSig = new SignatureHelper().verifyAssertionSignature(fullCertificate);
		}

		if (!SubjectProperties.isEmpty()) {
			validProps = true;
		}

	}
}

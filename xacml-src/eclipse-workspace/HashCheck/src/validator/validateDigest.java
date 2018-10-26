package validator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.Key;
import java.security.KeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.pmw.tinylog.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import core.Config;
import core.Utils;

public class validateDigest {
/**
 * 
 * This class only exists because validating the digest of a SAML assertion on its own
 * is not something directly supported by the opensaml library.
 * 
 */

	public static void main(String[] args) throws Exception {
		String xmlString = Utils.readFile("C:\\users\\jrfleming3\\desktop\\sable_baddig.xml");
		System.out.println("valid digest: "+ new validateDigest().validDigest(xmlString));

	}

	public boolean validDigest(String xml) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document document = null;
		factory.setNamespaceAware(true);
		try {
			builder = factory.newDocumentBuilder();
			document = builder.parse(new InputSource(new StringReader(xml)));
		} catch (Exception e) {
			e.printStackTrace();
		}

		NodeList nl = document.getChildNodes();
		Node ass = null;
		for(int i = 0; i<nl.getLength(); i++){
			if(nl.item(i).getNodeName().equals("saml2:Assertion"))
				ass = nl.item(i);
		}
		Node sig = null;
		nl = ass.getChildNodes();
		for(int i = 0; i<nl.getLength(); i++){
			if(nl.item(i).getNodeName().equals("ds:Signature"))
				sig = nl.item(i);
		}

		String issuer = ass.getFirstChild().getTextContent();

		DOMValidateContext valContext = new DOMValidateContext(new KeyValueKeySelector(issuer), sig);
		valContext.setIdAttributeNS((Element) sig.getParentNode(), null, "ID");
		XMLSignatureFactory fafacctory = XMLSignatureFactory.getInstance("DOM");
		XMLSignature signature = null;
		try{
			signature = fafacctory.unmarshalXMLSignature(valContext);
		}catch (Exception e){
			Logger.error("bad XML when attempting to validate digest!");
			return false;
		}
		try {
			return ((Reference) signature.getSignedInfo().getReferences().get(0)).validate(valContext);
		} catch (XMLSignatureException e) {
			e.printStackTrace();
			return false;
		}
	}

}

class SimpleKeySelectorResult implements KeySelectorResult {
	private PublicKey pk;

	SimpleKeySelectorResult(PublicKey pk) {
		this.pk = pk;
	}

	@Override
	public Key getKey() {
		return this.pk;
	}
}

class KeyValueKeySelector extends KeySelector {
	
	private String issuer = null;
	KeyValueKeySelector(String issuer){
		super();
		this.issuer = issuer;
	}

	public KeySelectorResult select(KeyInfo keyInfo, KeySelector.Purpose purpose, AlgorithmMethod method, XMLCryptoContext context) {
		KeyValue keyValue = null;
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(new FileInputStream(Config.Keystore.keystore_path),Config.Keystore.store_pass.toCharArray());

			PublicKey dsaPublicKey = keyStore.getCertificate(issuer).getPublicKey();
			KeyInfoFactory factory = KeyInfoFactory.getInstance("DOM");

			keyValue = factory.newKeyValue(dsaPublicKey);
			return new SimpleKeySelectorResult(keyValue.getPublicKey());
		} catch (KeyException | KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			return null;
		}

	}

	static boolean algEquals(String algURI, String algName) {
		if (algName.equalsIgnoreCase("DSA") && algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) {
			return true;
		} else if (algName.equalsIgnoreCase("RSA") && algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1)) {
			return true;
		} else {
			return false;
		}
	}
}

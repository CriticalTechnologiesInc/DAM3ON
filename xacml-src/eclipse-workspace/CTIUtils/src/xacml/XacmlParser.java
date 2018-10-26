package xacml;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.pmw.tinylog.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Description: This class is responsible for parsing out pieces of information
 * from an XACML request.
 * 
 * 
 * @author Justin Fleming - fleminjr@critical.com Jeremy Fields -
 *         fieldsjd@critical.com
 *
 */
public class XacmlParser {

	/**
	 * Description: This class pulls out specified Advice contained within a
	 * piece of XACML.
	 * 
	 * @param xml
	 *            String representing the full XACML and/or XACML+SAML request.
	 * @param attId
	 *            String of the AttributeId to search for
	 * @return ArrayList<String> of all Advice that matched the given
	 *         AttributeId
	 */
	public static ArrayList<String> getAdvice(String xml, String attId) {

		// Standard xml builder
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			return null;
		}

		Document doc = null;
		try {
			InputSource is = new InputSource(new StringReader(xml));
			doc = dBuilder.parse(is);
		} catch (SAXException e) {
			return null;
		} catch (IOException e) {
			return null;
		}

		// optional, but recommended
		// read this -
		// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();

		// Start by grabbing the Obligations node, which lower down contains what
		// we're looking for
		// This is a LIST of all "Obligations" nodes. There should only be 1, so we
		// grab the first one.
		NodeList nlist = doc.getElementsByTagName("AssociatedAdvice");
		NodeList ObList;
		try {
			ObList = nlist.item(0).getChildNodes();
		} catch (Exception e) {
			return null;
		}

		// Find the <Obligation> tags
		List<Node> Ob = new ArrayList<Node>();
		for (int i = 0; i < ObList.getLength(); i++) {
			if (ObList.item(i).getNodeName().toLowerCase().equals("advice")) {
				Ob.add(ObList.item(i));
			}
		}

		if (Ob.isEmpty()) {
			return null;
		}

		ArrayList<String> result = new ArrayList<String>();

		for (int i = 0; i < Ob.size(); i++) {
			NodeList obChildren = Ob.get(i).getChildNodes();
			for (int j = 0; j < obChildren.getLength(); j++) {
				if (obChildren.item(j).getNodeName().toLowerCase().equals("attributeassignment")
						&& obChildren.item(j).getAttributes().getNamedItem("AttributeId").getFirstChild().getNodeValue()
								.toLowerCase().equals(attId)) {
					result.add(obChildren.item(j).getTextContent());
				}
			}
		}

		if (result.isEmpty()) {
			return null;
		}

		return result;
	}


	/**
	 * Parsing method to extract the action || subject || resource in a response
	 * 
	 * @param xml
	 *            String of the plain text XACML request
	 * @param cat
	 *            String of the category name of the <Attributes> tag to match
	 * @param attid
	 *            String of the AttributeId to search for (i.e. urn:...:subject-id)
	 * @return String of the found match, or null if no match
	 */
	public static String getSar(String xml, String cat, String attid) {
		// Standard xml builder

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		Document doc = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xml));
			doc = dBuilder.parse(is);

			// optional, but recommended read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			// Get ALL <Attributes> nodes
			NodeList Attributes = doc.getElementsByTagName("Attributes");

			// For each <Attributes> node ...
			for (int i = 0; i < Attributes.getLength(); i++) {
				// If it matches the category we're looking for...
				if (Attributes.item(i).getAttributes().getNamedItem("Category").getNodeValue().equals(cat)) {
					NodeList attList = Attributes.item(i).getChildNodes();
					// Then for each of its children ...
					for (int j = 0; j < attList.getLength(); j++) {
						// If that childs AttributeId matches...
						if(attList.item(j).getAttributes().getNamedItem("AttributeId").getNodeValue().equals(attid)) {
							// Then it's the one we're looking for, so return it.
							return attList.item(j).getFirstChild().getTextContent();
						}
					}
					
				}
			}
			
			return null;
		} catch (ParserConfigurationException | SAXException | IOException | NullPointerException e1) {
			Logger.error(e1);
			return null;
		}

	}

	/**
	 * Parsing method to extract the decision in a response
	 * 
	 * @param xml
	 *            String of plaintext XACML request
	 * @return String of the found decision, or null if not found.
	 */
	public static String getDecisionValueString(String xml) {
		// Standard xml builder
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			return null;
		}

		Document doc = null;
		try {
			InputSource is = new InputSource(new StringReader(xml));
			doc = dBuilder.parse(is);
		} catch (SAXException e) {
			return null;
		} catch (IOException e) {
			return null;
		}

		doc.getDocumentElement().normalize();

		NodeList nlist = doc.getElementsByTagName("Decision");
		return nlist.item(0).getTextContent();
	}
}
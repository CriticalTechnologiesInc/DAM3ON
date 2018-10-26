package core;

/**
 * @author 
 * Justin Fleming - fleminjr@critical.com
 * Jeremy Fields - fieldsjd@critical.com
 *
 */
public class CTIConstants {
	
	
	public final static String SUBJECT_CATEGORY = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";
	public final static String RESOURCE_CATEGORY = "urn:oasis:names:tc:xacml:3.0:attribute-category:resource";
	public final static String ACTION_CATEGORY = "urn:oasis:names:tc:xacml:3.0:attribute-category:action";
	public final static String ENVIRONMENT_CATEGORY = "urn:oasis:names:tc:xacml:3.0:attribute-category:environment";
													  
	public final static String SUBJECT_ATTRIBUTEID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";
	public final static String ACTION_ATTRIBUTEID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
	public final static String RESOURCE_ATTRIBUTEID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
	
	public final static String XACML2_XMLNS = "urn:oasis:names:tc:xacml:2.0:context:schema:os";
	public final static String XACML3_XMLNS = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17";
	public final static String STRING = "http://www.w3.org/2001/XMLSchema#string";
	
	public final static String XMLVERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	public final static String REQOPEN = "<Request xmlns=\"urn:oasis:names:tc:xacml:2.0:context:schema:os\">";
	public final static String REQCLOSE = "</Request>";
	public final static String SUBOPEN = "<Subject>";
	public final static String SUBCLOSE = "</Subject>";
	public final static String SUBATTIDOPEN = "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">";
	public final static String RESOPEN = "<Resource>";
	public final static String RESCLOSE = "</Resource>";
	public final static String RESATTIDOPEN = "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">";
	public final static String ACTOPEN = "<Action>";
	public final static String ACTCLOSE = "</Action>";
	public final static String ACTATTIDOPEN = "<Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">";
	public final static String ATTVALOPEN = "<AttributeValue>";
	public final static String ATTVALCLOSE = "</AttributeValue>";
	public final static String ATTCLOSE = "</Attribute>";
	public final static String ENVOPEN = "<Environment>";
	public final static String ENVCLOSE = "</Environment>";
	public final static String ENVATTOPENSTART = "<Attribute AttributeId=\"";
	public final static String ENVATTOPENEND = "\" DataType=\"http://www.w3.org/2001/XMLSchema#string\">";
	
	public static final String POLICY_SCHEMA_PROPERTY = "com.sun.xacml.PolicySchema";
	public static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	public static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	public static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
}

package critical;

import java.awt.Color;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import xEdit.XmlTextPane;

public class GUIUtils {
	// CONSTANTS
	
	// Categories
	private final static String SUBJECT_CATEGORY = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";
	private final static String RESOURCE_CATEGORY = "urn:oasis:names:tc:xacml:3.0:attribute-category:resource";
	private final static String ACTION_CATEGORY = "urn:oasis:names:tc:xacml:3.0:attribute-category:action";
	private final static String ENVIRONMENT_CATEGORY = "urn:oasis:names:tc:xacml:3.0:attribute-category:environment";
	
	// SAR Attribute ID's
	private final static String SUBJECT_ATTRIBUTEID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";
	private final static String ACTION_ATTRIBUTEID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
	private final static String RESOURCE_ATTRIBUTEID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
	
	// Other
	private final static String XACML3_XMLNS = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17";
	private final static String DEFAULT_COMBINING_ALGO_ID = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides";
	private final static String DATATYPE_STRING = "http://www.w3.org/2001/XMLSchema#string";
	private final static String FUN_NS = "urn:oasis:names:tc:xacml:1.0:function:";
	
	// Functions
	private final static String FUN_STRING_EQUAL = FUN_NS + "string-equal";
	private final static String FUN_STRING_IS_IN = FUN_NS + "string-is-in";
	private final static String FUN_STRING_ONE_AND_ONLY = FUN_NS + "string-one-and-only";
	private final static String FUN_STRING_BAG = FUN_NS + "string-bag";
	private final static String FUN_STRING_BAG_SIZE = FUN_NS + "string-bag-size";
	private final static String FUN_STRING_SUBSET = FUN_NS + "string-subset";
	private final static String FUN_STRING_ALOMO = FUN_NS + "string-at-least-one-member-of";
	private final static String FUN_INT_EQUAL = FUN_NS + "integer-equal";
	private final static String FUN_AND = FUN_NS + "and";
	
	// Attributes
	private final static String DATATYPE = "DataType";
	private final static String ATT_ID = "AttributeId";
	private final static String FUN_ID = "FunctionId";
	private final static String ADVICEID = "AdviceId";
	private final static String MUST_BE_PRESENT = "MustBePresent";

	// Tags
	private final static String ADVICE_EXPS = "AdviceExpressions";
	private final static String ADVICE_EXP = "AdviceExpression";
	private final static String APPLY = "Apply";
	private final static String ATT_VAL = "AttributeValue";
	private final static String ATT_DES = "AttributeDesignator";
	private final static String ATT_ASS_EXP = "AttributeAssignmentExpression";
	private final static String TARGET = "Target";
	private final static String RULE = "Rule";
	private final static String ANYOF = "AnyOf";
	private final static String ALLOF = "AllOf";
	private final static String MATCH = "Match";
	private final static String CONDITION = "Condition";
	private final static String CATEGORY = "Category";
	// END CONSTANTS
	
	static class guiObj{
		static ArrayList<String> resources = new ArrayList<String>();
		static ArrayList<String> secprops = new ArrayList<String>();
		static ArrayList<String> authusers = new ArrayList<String>();
		static ArrayList<String> locations = new ArrayList<String>();
		static HashMap<String, ArrayList<String>> extras = new HashMap<String, ArrayList<String>>();
		static String name;
		static String secmod;
	}
	
	static class dbObj{
		static String username;
		static char[] password;
		static String location;
		static String table;
	}
	
	
	static String buildXacml(boolean format){
		
		Document root = DocumentHelper.createDocument();
		Namespace ns = new Namespace("", XACML3_XMLNS);
		
		Element policy = root.addElement(new QName("Policy", ns));
		
		String tmp = "";
		for(String res : GUIUtils.guiObj.resources)
			tmp += res+"_";
		tmp += "policy";
		GUIUtils.guiObj.name = tmp + ".xml";
		
		policy.addAttribute("xmlns:xacml", XACML3_XMLNS);
		policy.addAttribute("PolicyId", tmp);
		policy.addAttribute("RuleCombiningAlgId", DEFAULT_COMBINING_ALGO_ID);
		
		policy = buildTarget(policy);
		policy = buildRule(policy);
		policy = buildDenyAll(policy);
		policy = buildAdvice(policy);

		if(format)
			return format(root);
		else
			return root.asXML();
	}

	
	private static Element buildAdvice(Element policy){
		Element AdviceExpressions = policy.addElement(ADVICE_EXPS);
		Element AdviceExpression_permit = AdviceExpressions.addElement(ADVICE_EXP)
				.addAttribute(ADVICEID, "resource_url_advice")
				.addAttribute("AppliesTo", "Permit");
		
		for(String loc : GUIUtils.guiObj.locations){
			Element AttributeAssignmentExpression = AdviceExpression_permit.addElement(ATT_ASS_EXP).addAttribute(ATT_ID,  "resource_url");
			Element AttributeValue = AttributeAssignmentExpression.addElement(ATT_VAL).addAttribute(ATT_ID,  "resource_url").addAttribute(DATATYPE,  DATATYPE_STRING);
			AttributeValue.setText(loc);
		}
		
		boolean auth = false, attest = false;
		
		switch (GUIUtils.guiObj.secmod){
			case "authentication":
				auth = true;
				break;
			case "attestation":
				attest = true;
				break;
			case "auth+attest":
				auth = attest = true; 
				break;
		}
		
		Element AdviceExpression_deny = AdviceExpressions.addElement(ADVICE_EXP)
				.addAttribute(ADVICEID, "deny_adv")
				.addAttribute("AppliesTo", "Deny");
		
		Element AttributeAssignmentExpression_auth = AdviceExpression_deny.addElement(ATT_ASS_EXP).addAttribute(ATT_ID,  "auth");
		Element AttributeValue_auth = AttributeAssignmentExpression_auth.addElement(ATT_VAL).addAttribute(ATT_ID,  "auth").addAttribute(DATATYPE,  DATATYPE_STRING);
		AttributeValue_auth.setText(Boolean.toString(auth));
		
		Element AttributeAssignmentExpression_attest = AdviceExpression_deny.addElement(ATT_ASS_EXP).addAttribute(ATT_ID,  "attest");
		Element AttributeValue_attest = AttributeAssignmentExpression_attest.addElement(ATT_VAL).addAttribute(ATT_ID,  "attest").addAttribute(DATATYPE,  DATATYPE_STRING);
		AttributeValue_attest.setText(Boolean.toString(attest));
		
		if(attest){
			Element AttributeAssignmentExpression_pcr17 = AdviceExpression_deny.addElement(ATT_ASS_EXP).addAttribute(ATT_ID,  "pcr_index");
			Element AttributeValue_pcr17 = AttributeAssignmentExpression_pcr17.addElement(ATT_VAL).addAttribute(ATT_ID,  "pcr_index").addAttribute(DATATYPE,  DATATYPE_STRING);
			AttributeValue_pcr17.setText("17");
			
			Element AttributeAssignmentExpression_pcr19 = AdviceExpression_deny.addElement(ATT_ASS_EXP).addAttribute(ATT_ID,  "pcr_index");
			Element AttributeValue_pcr19 = AttributeAssignmentExpression_pcr19.addElement(ATT_VAL).addAttribute(ATT_ID,  "pcr_index").addAttribute(DATATYPE,  DATATYPE_STRING);
			AttributeValue_pcr19.setText("19");
		}
	
		return policy;
	}
	
	private static Element buildDenyAll(Element policy){
		policy.addElement(RULE).addAttribute("Effect","Deny").addAttribute("RuleId", "Deny_all_others");
		return policy;
	}
	
	private static Element buildTarget(Element policy){
		Element Target = policy.addElement(TARGET);
		Element AnyOf = Target.addElement(ANYOF);

		for(String resource_s : guiObj.resources){
			Element AllOf = AnyOf.addElement(ALLOF);
			Element Match = AllOf.addElement(MATCH).addAttribute("MatchId", FUN_STRING_EQUAL);

			Element AttributeValue = Match.addElement(ATT_VAL).addAttribute(DATATYPE,  DATATYPE_STRING);
			AttributeValue.setText(resource_s);
			
			Match.addElement(ATT_DES)
					.addAttribute(MUST_BE_PRESENT,  "false")
					.addAttribute(CATEGORY,  RESOURCE_CATEGORY)
					.addAttribute(ATT_ID,  RESOURCE_ATTRIBUTEID)
					.addAttribute(DATATYPE, DATATYPE_STRING);
		}
		
		return policy;
	}
	
	private static Element buildCustomCTIApply(Element condition, String key, ArrayList<String> values){
		
		for(String val : values){
			String[] bag = val.split("\\r\\n|\\n|\\r");
			
			Element Apply_custom = condition.addElement(APPLY).addAttribute(FUN_ID,  key);
			Element Apply_sb = Apply_custom.addElement(APPLY).addAttribute(FUN_ID,  FUN_STRING_BAG);
			
			for(String item : bag){
				Element av = Apply_sb.addElement(ATT_VAL);
				av.addAttribute(DATATYPE, DATATYPE_STRING);
				av.setText(item);
			}
			
			Element AttributeDesignator = Apply_custom.addElement(ATT_DES)
			.addAttribute(DATATYPE, DATATYPE_STRING)
			.addAttribute(MUST_BE_PRESENT, "false")
			.addAttribute(CATEGORY, ENVIRONMENT_CATEGORY);
			
			if(key.equals("ip-on-whitelist") || key.equals("ip-on-blacklist"))
				AttributeDesignator.addAttribute(ATT_ID, "ip");
			else if(key.equals("lat-long-in-polygon") || key.equals("lat-long-not-in-polygon"))
				AttributeDesignator.addAttribute(ATT_ID, "ip-lat-long");
			
		}
		return condition;
	}
	
	private static Element buildStringIsIn(Element condition, String key, ArrayList<String> values){
		for(String val : values){
			String[] bag = val.split("\\r\\n|\\n|\\r");
			
			Element Apply_sii = condition.addElement(APPLY).addAttribute(FUN_ID, FUN_STRING_IS_IN);
			Element Apply_soao = Apply_sii.addElement(APPLY).addAttribute(FUN_ID, FUN_STRING_ONE_AND_ONLY);
			
			Apply_soao.addElement(ATT_DES)
					.addAttribute(ATT_ID, key)
					.addAttribute(DATATYPE, DATATYPE_STRING)
					.addAttribute(MUST_BE_PRESENT, "false")
					.addAttribute(CATEGORY, ENVIRONMENT_CATEGORY);
			
			Element Apply_sb = Apply_sii.addElement(APPLY);
			Apply_sb.addAttribute(FUN_ID, FUN_STRING_BAG);
			
			for(String value : bag){
				Element av = Apply_sb.addElement(ATT_VAL);
				av.addAttribute(DATATYPE, DATATYPE_STRING);
				av.setText(value);
			}
		}
		return condition;
	}
	
	private static Element buildStringSubset(Element condition, String key, ArrayList<String> values){
		Element Apply_sss = condition.addElement(APPLY).addAttribute(FUN_ID, FUN_STRING_SUBSET);
		Element Apply_sb = Apply_sss.addElement(APPLY).addAttribute(FUN_ID,  FUN_STRING_BAG);
		
		for(String value : values){
			Element av = Apply_sb.addElement(ATT_VAL);
			av.addAttribute(DATATYPE, DATATYPE_STRING);
			av.setText(value);
		}
		
		Apply_sss.addElement(ATT_DES)
		.addAttribute(ATT_ID, key)
		.addAttribute(DATATYPE, DATATYPE_STRING)
		.addAttribute(MUST_BE_PRESENT, "false")
		.addAttribute(CATEGORY, ENVIRONMENT_CATEGORY);
		
		return condition;
	}
	
	private static Element buildStringAtLeastOneMemberOf(Element condition, String key, ArrayList<String> value){
		for(String val : value){
			String[] bag = val.split("\\r\\n|\\n|\\r");
			
			Element Apply_salomo = condition.addElement(APPLY).addAttribute(FUN_ID, FUN_STRING_ALOMO);
			Element Apply_sb = Apply_salomo.addElement(APPLY).addAttribute(FUN_ID, FUN_STRING_BAG);
			
			for(String s : bag){
				Element av = Apply_sb.addElement(ATT_VAL);
				av.addAttribute(DATATYPE, DATATYPE_STRING);
				av.setText(s);
			}
			
			Apply_salomo.addElement(ATT_DES)
			.addAttribute(ATT_ID, key)
			.addAttribute(DATATYPE, DATATYPE_STRING)
			.addAttribute(MUST_BE_PRESENT, "false")
			.addAttribute(CATEGORY, ENVIRONMENT_CATEGORY);
			
		}
		return condition;
	}
	
	private static Element buildRule(Element policy){
		Element Rule = policy.addElement(RULE).addAttribute("Effect", "Permit").addAttribute("RuleId", "access_rule");
		Element Target = Rule.addElement(TARGET);
		Element AnyOf = Target.addElement(ANYOF);
		Element AllOf = AnyOf.addElement(ALLOF);
		Element Match = AllOf.addElement(MATCH).addAttribute("MatchId", FUN_STRING_EQUAL);

		Element AttributeValue = Match.addElement(ATT_VAL).addAttribute(DATATYPE,  DATATYPE_STRING);
		AttributeValue.setText("access");
		
		Match.addElement(ATT_DES)
				.addAttribute(MUST_BE_PRESENT,  "false")
				.addAttribute(CATEGORY,  ACTION_CATEGORY)
				.addAttribute(ATT_ID,  ACTION_ATTRIBUTEID)
				.addAttribute(DATATYPE, DATATYPE_STRING);
		
		Element condition = null;
		Element Apply_and_top = null;
		
		if(!GUIUtils.guiObj.secprops.isEmpty()){
			condition = Rule.addElement(CONDITION);
			Apply_and_top = condition.addElement(APPLY).addAttribute(FUN_ID, FUN_AND);
			
			Element Apply_ie = Apply_and_top.addElement(APPLY);
			Apply_ie.addAttribute(FUN_ID, FUN_INT_EQUAL);
			
			Element Apply_ie_sbs = Apply_ie.addElement(APPLY).addAttribute(FUN_ID, FUN_STRING_BAG_SIZE);
			Apply_ie_sbs.addElement(ATT_DES)
				.addAttribute(MUST_BE_PRESENT,  "false")
				.addAttribute(CATEGORY,  ENVIRONMENT_CATEGORY)
				.addAttribute(ATT_ID,  "hashes")
				.addAttribute(DATATYPE, DATATYPE_STRING);
			
			Element AttributeValue_1 = Apply_ie.addElement(ATT_VAL).addAttribute(DATATYPE, DATATYPE_STRING);
			AttributeValue_1.setText("1");
			
			Apply_and_top = buildStringSubset(condition, "get-props-from-hashes", GUIUtils.guiObj.secprops);
		}
		
		if(GUIUtils.guiObj.secmod.equals("authentication") || GUIUtils.guiObj.secmod.equals("auth+attest")){
			if(condition == null){
				condition = Rule.addElement(CONDITION);
				Apply_and_top = condition.addElement(APPLY).addAttribute(FUN_ID, FUN_AND);
			}
			
			Element Apply_pgp = Apply_and_top.addElement(APPLY).addAttribute(FUN_ID, "valid-pgp");
			
			Apply_pgp.addElement(ATT_DES)
			.addAttribute(MUST_BE_PRESENT,  "false").addAttribute(DATATYPE, DATATYPE_STRING)
			.addAttribute(ATT_ID,  SUBJECT_ATTRIBUTEID).addAttribute(CATEGORY,  SUBJECT_CATEGORY);
			
			Apply_pgp.addElement(ATT_DES)
			.addAttribute(MUST_BE_PRESENT,  "false").addAttribute(DATATYPE, DATATYPE_STRING)
			.addAttribute(ATT_ID,  ACTION_ATTRIBUTEID).addAttribute(CATEGORY,  ACTION_CATEGORY);
			
			Apply_pgp.addElement(ATT_DES)
			.addAttribute(MUST_BE_PRESENT,  "false").addAttribute(DATATYPE, DATATYPE_STRING)
			.addAttribute(ATT_ID,  RESOURCE_ATTRIBUTEID).addAttribute(CATEGORY,  RESOURCE_CATEGORY);
			
			Apply_pgp.addElement(ATT_DES)
			.addAttribute(MUST_BE_PRESENT,  "false").addAttribute(DATATYPE, DATATYPE_STRING)
			.addAttribute(ATT_ID,  "pgpsig").addAttribute(CATEGORY,  ENVIRONMENT_CATEGORY);
			
		}
		
		if(!guiObj.extras.isEmpty()){
			if(condition == null){
				condition = Rule.addElement(CONDITION);
				Apply_and_top = condition.addElement(APPLY).addAttribute(FUN_ID, FUN_AND);	
			}
			for(String se : guiObj.extras.keySet()){
				if(getSalomoList().contains(se)){ // if doing string-at-least-one-member-of ...
					Apply_and_top = buildStringAtLeastOneMemberOf(Apply_and_top, se, guiObj.extras.get(se));
				} else if(getStringIsInList().contains(se)){
					Apply_and_top = buildStringIsIn(Apply_and_top, se, guiObj.extras.get(se));
				} else if(customFunctionsList().contains(se)){
					Apply_and_top = buildCustomCTIApply(Apply_and_top, se, guiObj.extras.get(se));
				}
			}
		}
		
		return policy;
	}
	
	 static String format(Document root) {
    	OutputFormat format = OutputFormat.createPrettyPrint();
    	format.setIndent("        ");
    	
    	StringWriter out = new StringWriter();
    	
    	XMLWriter writer = new XMLWriter(out, format);
    	try {
			writer.write(root);
			writer.close();
		} catch (IOException e) {
		}
        	
        return out.toString();
    }
	
	
	static void showPreview(){
		JFrame frame = new JFrame();
		
		XmlTextPane xtp = new XmlTextPane();
		xtp.setText(GUIUtils.buildXacml(true));
		xtp.setBackground(new Color(240,240,240));
		JScrollPane scroll = new JScrollPane(xtp, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		xtp.setEditable(false); // FYI you -could- allow editing, but idk about that
		
		xtp.setCaretPosition(0); // makes it scroll to top by default.. doesn't otherwise
		
	   	frame.add(scroll);

	   	frame.setMinimumSize(new Dimension(1100,725));
	   	frame.setResizable(true);
	   	frame.setLocationRelativeTo(null);
	   	frame.setTitle("XACML Preview");
	   	frame.setVisible(true);
	}
	
	static void saveFile(File f, String content){
		try{
			PrintWriter writer = new PrintWriter(f);
			writer.write(content);
			writer.close();
		}catch (Exception e){
			// what can ya do
		}
	}
	
	private static void passwordWipe(){
		for(int i=0; i<GUIUtils.dbObj.password.length; i++)
			GUIUtils.dbObj.password[i] = 0;
	}
	
	static boolean testDbConnection(){
		Properties connectProperties = new Properties();

		connectProperties.put("user", GUIUtils.dbObj.username);
		connectProperties.put("password", new String(GUIUtils.dbObj.password));
		
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			// Open a connection
			conn = DriverManager.getConnection("jdbc:mysql://" + GUIUtils.dbObj.location, connectProperties);

			String queryString = "select 1";

			stmt = conn.prepareStatement(queryString);
			ResultSet rs = stmt.executeQuery();

			rs.close();
			stmt.close();
			conn.close();
			return true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,e.getMessage());
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
				JOptionPane.showMessageDialog(null,se2.getMessage());
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				JOptionPane.showMessageDialog(null,se.getMessage());
			} // end finally try
		}
		return false;
	}
	
	static boolean uploadToDb(){

		Properties connectProperties = new Properties();

		connectProperties.put("user", GUIUtils.dbObj.username);
		connectProperties.put("password", new String(GUIUtils.dbObj.password));
		passwordWipe();
		Connection conn = null;
		PreparedStatement stmt = null;
		
		String blob = buildXacml(true);
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			// Open a connection
			conn = DriverManager.getConnection("jdbc:mysql://" + GUIUtils.dbObj.location,
					connectProperties);

			String queryString = "INSERT INTO " + GUIUtils.dbObj.table + " values(?, ?)";
			stmt = conn.prepareStatement(queryString);
			stmt.setString(1,GUIUtils.guiObj.name);
			
			InputStream stream = new ByteArrayInputStream(blob.getBytes(StandardCharsets.UTF_8));
			stmt.setBlob(2, stream);
			
			stmt.executeUpdate();

			stmt.close();
			conn.close();
			return true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,e.getMessage());
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
				JOptionPane.showMessageDialog(null,se2.getMessage());
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				JOptionPane.showMessageDialog(null,se.getMessage());
			} // end finally try
		}
		return false;
	}
	
	
	static boolean validatePolicyInfo(){
		String mod = GUIUtils.guiObj.secmod;
		
		if(mod.equals("none")){
			if(GUIUtils.guiObj.locations.isEmpty() || GUIUtils.guiObj.resources.isEmpty())
				return false;
		}
			
		if(mod.equals("authentication")){
			if(GUIUtils.guiObj.locations.isEmpty() || GUIUtils.guiObj.resources.isEmpty() || GUIUtils.guiObj.authusers.isEmpty())
				return false;
		}
		
		if(mod.equals("attestation")){
			if(GUIUtils.guiObj.locations.isEmpty() || GUIUtils.guiObj.resources.isEmpty() || GUIUtils.guiObj.secprops.isEmpty())
				return false;
		}
		
		if(mod.equals("auth+attest")){
			if(GUIUtils.guiObj.locations.isEmpty() || GUIUtils.guiObj.resources.isEmpty() || GUIUtils.guiObj.secprops.isEmpty() || GUIUtils.guiObj.authusers.isEmpty())
				return false;
		}
		
		return true;
	}

	// these all fit the model of:
	// <string-at-least-one-member-of>
	//		<string-bag>
    //			<value1>
	//			<valueN>
	//		</string-bag>
	// 		<environmentAttributeDesignator AttributeId="THE THINGS LISTED BELOW">
	// </string-at-least-one-member-of>
	private static ArrayList<String> getSalomoList(){ 
		ArrayList<String> al = new ArrayList<String>();
		al.add("ip-zipcode");
		al.add("ip-timezone");
		al.add("ip-region");
		al.add("ip-country");
		al.add("ip-lat-long");
		al.add("ip-city");
		al.add("ua-device-type");
		al.add("ua-os-name");
		al.add("ua-os-manufacturer");
		al.add("ua-browser-version");
		al.add("ua-browser-render-engine");
		al.add("ua-browser-name");
		al.add("ua-browser-manufacturer");
		al.add("ua-browser-type");
		return al;
	}
	
	private static ArrayList<String> getStringIsInList(){
		ArrayList<String> al = new ArrayList<String>();
		al.add("pcr17");
		return al;
	}
	
	
	// These all follow the format of:
	// <apply funid="ONE OF THE THINGS LISTED BELOW">
	//	 <apply string bag>
	//		<bunch of att vals>
	//	 </apply>
	//	 <env att des>
	// </apply>
	private static ArrayList<String> customFunctionsList(){
		ArrayList<String> al = new ArrayList<String>();
		al.add("ip-on-whitelist");
		al.add("ip-on-blacklist");
		al.add("lat-long-in-polygon");
		al.add("lat-long-not-in-polygon");
		return al;
	}
	
}


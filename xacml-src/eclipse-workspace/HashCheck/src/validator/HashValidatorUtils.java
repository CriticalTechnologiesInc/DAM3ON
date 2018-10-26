package validator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;

import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.pmw.tinylog.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import core.Config;
import core.Utils;
import xacml.PropTypes;
import xacml.Property;

public class HashValidatorUtils {

	
	// maxlevel and maxProperties define the width and depth of the recursion
	private final static int maxlevel = Config.Certificates.depth; // Maximum recursion depth
	private final static int maxProperties = Config.Certificates.max_properties; // Maximum number of properties allowed per certificate
	private final static boolean acceptingUntrusted = Config.Certificates.accepting_untrusted; // Whether we follow references in untrusted certificates
	private final static boolean addLowCertsToDB = Config.Certificates.add_evidence; // Whether we add evidence certificates to our cache DB
	
	
	// Main is for DEMO'ing & testing. It serves no other purpose
	public static void main(String[] args) {
		String url = "https://ctidev4.critical.com/certs/guard.xml";
		CTICertificate cert;
		String hash = "ee8ca7a80229e38588e5a1062a2320c6c372a097";
		
		try {
			cert = new CTICertificate(new URL(url));
		} catch (Exception e) {
			Logger.error(e);
			cert = null;
		}
		
		if(cert != null)
			processCertificateChain(cert, hash);
		else
			System.out.println("cert was NULL!");

	}

	/**
	 * Description: This function serves to determine what type, if any, a given
	 * certificate is.
	 * 
	 * @param cert A certificate as a String
	 * @return a String indicating the type of certificate (or "idk" if indeterminate)
	 */
	static String whatTypeOfCertificate(String cert){
		try{
	        // Initialize the library
	        DefaultBootstrap.bootstrap(); 
	         
	        // Get parser pool manager
	        BasicParserPool ppMgr = new BasicParserPool();
	        ppMgr.setNamespaceAware(true);
	         
	        // Parse metadata file
	        Document inCommonMDDoc = ppMgr.parse(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
	        Element metadataRoot = inCommonMDDoc.getDocumentElement();
	         
	        // Get apropriate unmarshaller
	        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
	        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(metadataRoot);
	        
	        // Cast XMLObject to an Assertion object
	        Assertion ass = (Assertion) unmarshaller.unmarshall(metadataRoot);
	        
	        // Get the first AttributeStatement in AttributeStatements (there should only be 1 in a valid cert)
	        AttributeStatement as = ass.getAttributeStatements().get(0);
	        
	        // Loop over each Attribute of the AttributeStatement. If we find one that matches, return. If we don't find any, return "idk"
	        for(Attribute att : as.getAttributes()){
	        	if(att.getName().equals("cert-info")){
	        		return "cert";
	        	}else if(att.getName().equals("proof-info")){
	        		return "proof";
	        	}else if(att.getName().equals("insurance-info")){
	        		return "insurance";
	        	}else if(att.getName().equals("pentest-info")){
	        		return "pentest";
	        	}
	    	}
	        
	        return "idk";
        
		}catch(Exception e){
			Logger.error("Failure in determining certificate type. Likely malformed or invalid XML");
			Logger.error(e);
			return "idk";
		}
		
	}
	

	/**
	 * Description: This is the primary, high level function for potentially recursing on a certificate chain.
	 * 
	 * This function serves as a wrapper for two primary, lower functions.
	 * 1. findAllValidCerts which recursively finds all valid certificates to be processed (i.e. certs worth looking at)
	 * 2. handleNewCert which by starting with a certificate, recurses in attempts of verifying each assertion of the original certificate
	 * 
	 * @param c is a CTICertificate object representing the original certificate provided by an end user
	 * @param origHash is a String of the hash of the subject of the original certificate. This is used to ensure that we only consider evidence from
	 * subsequent certificates that are about the same subject
	 */
	static void processCertificateChain(CTICertificate c, String origHash){
		Logger.info("About to potentially recurse on certificate " + c.getId());
		// If we're accepting untrusted, we need to follow untrusted refs to find all trusted certs,
		// then call handleNewCerts on each trusted cert.
		

		// Find all certificates to recurse on
		ArrayList<CTICertificate> tmp = new ArrayList<CTICertificate>();
		tmp = findAllValidCerts(c, 0, tmp, new ArrayList<CTICertificate>());
		Logger.debug("Found " + tmp.size() + " valid certificates (including duplicates");
		
		// Remove duplicates
		ArrayList<CTICertificate> certs = new ArrayList<CTICertificate>();
		for(CTICertificate cc : tmp){
			if(!certs.contains(cc))
				certs.add(cc);
		}
		
		Logger.debug("Found " + certs.size() + " valid certificates (without duplicates)");

		// For each valid & relevant certificate, call handleNewCert on it (which will attempt to walk the chain to verify each assertion)
		for(CTICertificate cert: certs){
			if(cert.getSubjectHashInfoHash().toLowerCase().equals(origHash.toLowerCase())){
				handleNewCert(cert);
			}else{
				System.out.println("cert "+cert.getId()+" was not about the original hash!");
			}
		}
		
		
		
	}
	
	
	/**
	 * Description:
	 * This merely builds a list of ALL valid certificates to later be dealt with. This potentially uses untrusted certs to find all trusted certs, which we
	 * can then treat trusted certs as normal (depends on config). This is a recursive function.
	 * 
	 * At a high level, this function recurses on the original certificate until a dead end is hit, or the recursion depth limit is hit.
	 * It will return a list of certificates that are: untampered with AND (trusted OR (untrusted && accepting untrusted)). This result is a subset of all
	 * possible certificates in the chain of the original certificate, which will then be used to again recurse on in attempts to verify all assertions.
	 * 
	 * @param cert CTICertificate object of a certificate to potentially recurse on
	 * @param depth int of the current recursion depth. This prevents run-away chains or cyclic dependencies
	 * @param list a running ArrayList\<CTICertificate\> of all certificates to ultimately return  
	 * @param visited a running ArrayList\<CTICertificate\> of all certificates that have already been visited.
	 * @return ArrayList\<CTICertificate\> of all certificates that we should try to verify assertions on
	 */
	public static ArrayList<CTICertificate> findAllValidCerts(CTICertificate cert, int depth, ArrayList<CTICertificate> list, ArrayList<CTICertificate> visited){
		
		if(cert == null)
			Logger.error("favc was called with a null cert!");
		
		// already visited?           tampered with?           recursion limit?           malicious number of properties?
		if(visited.contains(cert) || !cert.getValidDigest() || depth > maxlevel || cert.getSubjectProperties().size() > maxProperties)
			return list; 
		else // otherwise add this cert to visited
			visited.add(cert);
		
		Logger.debug("#########Running findAlLValidCerts on : " + cert.getId());
		
		// otherwise, we don't actually add the first cert. we only look at its refs to see if we add those
		if(depth == 0 && cert.getValidSig())
			list.add(cert);
		
		ArrayList<CTICertificate> tmp = new ArrayList<CTICertificate>();
		
		// For each reference...
		for(Property p : cert.getSubjectProperties()){
			CTICertificate c = null;
			
			if(p.type == PropTypes.REF){
				try{
					c = new CTICertificate(new URL(p.propertyValue));
				}catch(Exception e){
					c = null;
				}
				
				// if that reference is: not null, trusted, and isn't already in the list, then add it.
				// the "list" is all -trusted- certs to ultimately be returned.
				if(c != null && c.getValidSig()  && c.getValidDigest() && !list.contains(c)){
					Logger.debug("@@@@@@@@@@@@@@@List size: " + list.size() + ", now adding: " + c.getId());
					list.add(c);
				}
				
				// Add cert to the tmp list. "tmp" is a list of -some- referenced certs by the current cert,
				// so that we have a list of certs to follow
				if(c.getValidDigest() && (acceptingUntrusted || c.getValidSig())){
					tmp.add(c);
				}
			}
			
		}
		
		// For each cert in our list of certs to follow ...
		for(CTICertificate c : tmp){
			
			// recurse
			ArrayList<CTICertificate> another = findAllValidCerts(c, depth + 1, list, visited);
			
			// if the results aren't already in "list", add them
			for(CTICertificate anotherr : another){
				if(!list.contains(anotherr)){
					list.add(anotherr);
				}
			}
		}
		
		// return "list"
		return list;
	}
	
	
	/**
	 * Description:
	 * This is a higher level function that wraps the second recursive function.
	 * When given a certificate, it determines if it needs to add it to the database. If it does, it
	 * will recursively try to prove all assertions of ONLY the top level certificate. This is why the first
	 * recursive function is important in finding ALL certificates that need to be looked at.
	 * 
	 * @param cert a CTICertificate object to verify assertions on & add to the DB, if needed
	 * @return boolean success
	 */
	public static boolean handleNewCert(CTICertificate cert) {
		Logger.debug("running handleNewCert on : " + cert.getId());
		
		// Make sure certificate wasn't tampered with
		if (!cert.getValidDigest())
			return false;

		// If the certificate is trusted
		if (cert.getValidSig()) { 
			// If it isn't in the DB (i.e. we need to add it)
			if (needToAdd(cert.getHashOfCert())) {
				
				// Get the properties of this certificate
				ArrayList<String> baseCertProps = new ArrayList<String>();
				for (Property p : cert.getSubjectProperties()) {
					baseCertProps.add(p.propertyName);
				}
				// Recursively verify as many pieces of evidence as we can
				ArrayList<String> props = handleProperties(cert, 0);
				Logger.debug("about to retain all on a list of size: " + props.size());
				// Perform a set intersection of the original properties with those we could verify
				props.retainAll(baseCertProps);
				
				for (String s : props) {	 
					Logger.debug("Adding property: " + s + " for cert about: " + cert.getId());	
				}							

				// Add the final list of properties (and certificate) to the DB
				if (addEntryToFileDb(cert, props))
					return true;
				else
					return false;
				
			} else {
				return true;
			}
		} else { 			
			return false; 	
		} 					
	}

	/**
	 * Description:
	 * This should only be called with a CTICertificate that can be cast to a specific evidence certificate.
	 * 
	 * This method verifies, validates, and adds vetted properties to the DB.
	 * 
	 * @param cert a CTICertificate of a PentestCertificate, ProofCertificate, or InsuranceCertificate
	 * @param origHash String of the original hash of the top level subject that this program was intially called on
	 * @return boolean success
	 */
	public static boolean handleHighLevelEvidenceCert(CTICertificate cert, String origHash){
		// Make sure the certificate wasn't tampered with, is valid, and is about the original subject
		if(cert.getValidDigest() && cert.getValidSig() && cert.getSubjectHashInfoHash().equals(origHash)){
			// If we need to add it to the DB
			if(needToAdd(cert.getHashOfCert())){
				
				String type = whatTypeOfCertificate(cert.fullCertificate);
				
				if(type.equals("pentest"))
					addEntryToFileDb(cert, vetPentestCert((PentestCertificate)cert));
				else if(type.equals("insurance"))
					addEntryToFileDb(cert, vetInsuranceCert((InsuranceCertificate)cert));
				else if(type.equals("proof"))
					addEntryToFileDb(cert, vetProofCert((ProofCertificate)cert));
							
			}
			return true;
		}else{
			return false;	
		}
	}
	
	/**
	 * Description:
	 * This is the second of two recursive functions used when walking a chain of certificates. After the first function determines the
	 * list of valid certificates to investigate (possibly by following untrusted certificates), this function should then be called in a loop
	 * with all of those certificates
	 * 
	 * This function when given a certificate, will recursively try to prove all assertions of ONLY the original certificate
	 * 
	 * @param cert
	 * @param level
	 * @return
	 */
	private static ArrayList<String> handleProperties(CTICertificate cert, int level) {
		Logger.debug("Looking at cert: " + cert.getId());
		
		ArrayList<Property> props = cert.getSubjectProperties();
		ArrayList<String> validatedProperties = extractValidEvidenceFromAssertions(props, cert.getValidSig());
		
		Logger.debug("have props: " + validatedProperties + " for cert: " + cert.getId());
		
		HashMap<String, ArrayList<String>> hm = refsToHashMap(props, cert.getValidSig());
		
		for(String k : hm.keySet()){
			Logger.debug("Have reference : " + k + " For cert: " + cert.getId());
		}

		if(!cert.getValidDigest()){
			Logger.debug("INVALID DIGEST!");
			return new ArrayList<String>();
		}
		if(!(cert.getValidSig() || acceptingUntrusted)){
			Logger.debug("not (valid signature or accepting untrusted certificates!)");
			return validatedProperties;
		
		}else if(Config.Keystore.ultimate_trusted_issuers != null && Config.Keystore.ultimate_trusted_issuers.contains(cert.getIssuerName()) && cert.getValidSig()){
			ArrayList<String> tmp = new ArrayList<String>();
			for(Property p: cert.getSubjectProperties()){
				tmp.add(p.propertyName);
	 		}
			return tmp;
		
		}else{
			for (String key : hm.keySet()) {
				CTICertificate refCert = null;
				try {
					refCert = new CTICertificate(new URL(key));
				} catch (Exception e) {
					Logger.error(e);
				}
	
	
				if(!(level >= maxlevel)){ 
					Logger.debug("%%%%%%%%%%%%%%%%%%%%%%%about to recurse on cert: " + refCert.getId() + " as referenced by : " + cert.getId());
					// Recurse on the referenced certificate
					ArrayList<String> tmp = handleProperties(refCert, level + 1);
					
					// Set intersection on original properties of a cert AND those we could verify
					tmp.retainAll(hm.get(key));					
					// And those to our list of validated properties
					validatedProperties.addAll(tmp);
				}
			}
			return validatedProperties;
		}

	}

	/**
	 * Description:
	 * This is a simple function which converts a list of references to a hashmap of Reference --> [asserted properties]
	 * 
	 * @param props list of Property's
	 * @param valid whether the calling certificate was trusted
	 * @return a HashMap<String, ArrayList<String>> that maps asserted properties to a reference
	 */
	private static HashMap<String, ArrayList<String>> refsToHashMap(ArrayList<Property> props, boolean valid){
		HashMap<String, ArrayList<String>> hm = new HashMap<String, ArrayList<String>>();

		if(valid || acceptingUntrusted){
			for (Property p : props) {
				if (p.type == PropTypes.REF) {
					if (hm.containsKey(p.propertyValue)) {
						hm.get(p.propertyValue).add(p.propertyName);
					} else {
						ArrayList<String> a = new ArrayList<String>();
						a.add(p.propertyName);
						hm.put(p.propertyValue, a);
					}
				}
			}

		}
		return hm;
	}
	
	
	/**
	 * Description:
	 * This function will deal with direct links to evidence from a certificate. It will return a list of validated properties
	 * according to the rules for the three types of evidence certificates (Proof, Insurnace, Pentest).
	 * 
	 * @param props list of Property's from the original certificate
	 * @param valid boolean of whether the top certificate was validated
	 * @return ArrayList\<String\> of all validated properties
	 */
	private static ArrayList<String> extractValidEvidenceFromAssertions(ArrayList<Property> props, boolean valid){
		ArrayList<String> validatedProperties = new ArrayList<String>();
		
		// For each property P ....
		for (Property p : props) {
			
			// If the property has a reference to a Proof certificate ...
			if (p.type == PropTypes.PROOF && valid) {

				ProofCertificate pc = null;
				try {
					pc = new ProofCertificate(new URL(p.propertyValue));
				} catch (NoSuchAlgorithmException | IOException | ConfigurationException | BadDigestException | XMLParserException | UnmarshallingException | ParseException e) {
					Logger.error(e);
				}
				
				// If the certificate is untampered && trusted ...
				if(pc.getValidDigest() && pc.getValidSig()){

					// Vet that certificate, and add all of those properties to our validated properties list
					ArrayList<String> vettedProps = vetProofCert(pc);
					validatedProperties.addAll(vettedProps);
					
					// If the sys admin wishes to retain all evidence certs in the DB, we'll add them here - this only serves audit purposes
					if(addLowCertsToDB && needToAdd(pc.getHashOfCert())){
							addEntryToFileDb(pc, vettedProps);
					}
				}
				
			// If the property has a reference to an Insurance certificate ...
			} else if(p.type == PropTypes.INSURED && valid){
				InsuranceCertificate ic = null;
				try {
					ic = new InsuranceCertificate(new URL(p.propertyValue));
				} catch (NoSuchAlgorithmException | IOException | ConfigurationException | BadDigestException | XMLParserException | UnmarshallingException | ParseException e) {
					Logger.error(e);
				}
				
				// If the certificate is untampered && trusted ...
				if(ic.getValidDigest() && ic.getValidSig()){

					// Vet that certificate, and add all of those properties to our validated properties list
					ArrayList<String> vettedProps = vetInsuranceCert(ic);
					validatedProperties.addAll(vettedProps);
					
					// If the sys admin wishes to retain all evidence certs in the DB, we'll add them here - this only serves audit purposes
					if(addLowCertsToDB && needToAdd(ic.getHashOfCert())){
							addEntryToFileDb(ic, vettedProps);
					}
				}

			// If the property has a reference to an Pentest certificate ...
			}else if(p.type == PropTypes.PENTEST && valid){

				PentestCertificate pc = null;
				try {
					pc = new PentestCertificate(new URL(p.propertyValue));
				} catch (NoSuchAlgorithmException | IOException | ConfigurationException | BadDigestException | XMLParserException | UnmarshallingException | ParseException e) {
					Logger.error(e);
				}
				
				// If the certificate is untampered && trusted ...
				if(pc.getValidDigest() && pc.getValidSig()){

					// Vet that certificate, and add all of those properties to our validated properties list
					ArrayList<String> vettedProps = vetPentestCert(pc);
					validatedProperties.addAll(vettedProps);
					
					// If the sys admin wishes to retain all evidence certs in the DB, we'll add them here - this only serves audit purposes
					if(addLowCertsToDB && needToAdd(pc.getHashOfCert())){
							addEntryToFileDb(pc, vettedProps);
					}
				}
			}
		}
		
		return validatedProperties;
	}
	
	/**
	 * Description:
	 * This method contains InsuranceCertificate specific logic for determining what properties to accept.
	 * 
	 * In the future, this will contain logic like "how much money should a prop be insured for?" etc.
	 * 
	 * @param ic InsuranceCertificate
	 * @return ArrayList\<String\> of validated properties
	 */
	private static ArrayList<String> vetInsuranceCert(InsuranceCertificate ic){
		if(!ic.getValidDigest() || !ic.getValidSig())
			return new ArrayList<String>();

		ArrayList<String> als = new ArrayList<String>();
		for(Property p : ic.getSubjectProperties()){
			// do actual vetting here
			if(ic.getAmountInsured() >= Config.Certificates.required_insurance_amount){
				als.add(p.propertyName);	
			}
		}
		
		return als;
	}
	
	/**
	 * Description:
	 * This method contains ProofCertficate specific logic for determining what properties to accept.
	 * 
	 * In the future, this will contain logic for possibly running a proof assistant
	 * on provided theorem files
	 * 
	 * @param pc ProofCertficate
	 * @return ArrayList\<String\> of validated properties
	 */
	private static ArrayList<String> vetProofCert(ProofCertificate pc){
		if(!pc.getValidDigest() || !pc.getValidSig())
			return new ArrayList<String>();

		ArrayList<String> als = new ArrayList<String>();
		for(Property p : pc.getSubjectProperties()){
			// TODO do actual vetting here
			als.add(p.propertyName);
		}
		
		return als;
	}
	
	/**
	 * Description:
	 * This method contains PentestCertificate specific logic for determining what properties to accept.
	 * 
	 * In the future, this will contain logic for determining what pentesters we trust
	 * 
	 * @param pc PentestCertificate
	 * @return ArrayList\<String\> of validated properties
	 */
	private static ArrayList<String> vetPentestCert(PentestCertificate pc){
		if(!pc.getValidDigest() || !pc.getValidSig())
			return new ArrayList<String>();

		ArrayList<String> als = new ArrayList<String>();
		for(Property p : pc.getSubjectProperties()){
			// TODO do actual vetting here
			als.add(p.propertyName);
		}
		
		return als;
	}
	
	/**
	 * Description:
	 * This function when given a CTICertificate and a list of properties, will add it to our database.
	 * 
	 * @param cert a CTICertificate
	 * @param secprops_list a list of security properties that we have validated and accepted
	 * @return boolean success
	 */
	public static boolean addEntryToFileDb(CTICertificate cert, ArrayList<String> secprops_list) {
		
		Logger.debug("Running addEntryToFileDB on cert: " + cert.getId());

		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;

		Properties connectProperties = new Properties();

		connectProperties.put("user", Config.Databases.approved_file_db.user);
		connectProperties.put("password", Config.Databases.approved_file_db.password);
		connectProperties.put("useSSL", "true");
		connectProperties.put("verifyServerCertificate", "false"); // because we use self-signed certs
		System.setProperty("javax.net.ssl.keyStore", Config.Databases.SSL.keystore_path);
		System.setProperty("javax.net.ssl.keyStorePassword", Config.Databases.SSL.keystore_pw);

		try {
			Class.forName(Config.Databases.approved_file_db.jdbc_driver).newInstance();
			dbConnection = DriverManager.getConnection(Config.Databases.approved_file_db.db_url, connectProperties);
		} catch (Exception e) {
			Logger.error(e);
			return false;
		}

		String secProps = "";
		for (String s : secprops_list){
			secProps += s + ",";
		}

		secProps = secProps.substring(0, secProps.length() - 1);

		String insertTableSQL = "INSERT INTO files" + "(hashOfCert, binSha1, binName, description, secProps, notBefore, notAfter, certificate) VALUES" + "(?,?,?,?,?,?,?,?)";
		try {
			preparedStatement = dbConnection.prepareStatement(insertTableSQL);
			preparedStatement.setString(1, cert.getHashOfCert());
			preparedStatement.setString(2, cert.getSubjectHashInfoHash());
			preparedStatement.setString(3, cert.getSubjectName());
			preparedStatement.setString(4, cert.getSubjectDescription());
			preparedStatement.setString(5, secProps);
			preparedStatement.setString(6, cert.getValidityNotBeforeStr());
			preparedStatement.setString(7, cert.getValidityNotAfterStr());
			preparedStatement.setString(8, cert.getFullCertificate());

			preparedStatement.executeUpdate();
			return true;
		} catch (SQLException e) {
			Logger.error(e);
			return false;
		} finally {

			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					Logger.error("Database error: " + e);
				}
			}

			if (dbConnection != null) {
				try {
					dbConnection.close();
					return true;
				} catch (SQLException e) {
					Logger.error("Database error: " + e);
					return false;
				}
			}
		}
	}

	/**
	 * Description:
	 * This function will query the DB with the hash of the certificate to see if it exists already.
	 * If it does, it will return "false", otherwise "true"
	 * 
	 * @param certHash String of the hash of the certificate
	 * @return boolean whether it exists in the database or not
	 */
	public static boolean needToAdd(String certHash) {
		Properties connectProperties = new Properties();

		connectProperties.put("user", Config.Databases.approved_file_db.user);
		connectProperties.put("password", Config.Databases.approved_file_db.password);
		connectProperties.put("useSSL", "true");
		connectProperties.put("verifyServerCertificate", "false"); // because we use self-signed certs
		System.setProperty("javax.net.ssl.keyStore", Config.Databases.SSL.keystore_path);
		System.setProperty("javax.net.ssl.keyStorePassword", Config.Databases.SSL.keystore_pw);

		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			// Register JDBC driver mySql
			Class.forName(Config.Databases.approved_file_db.jdbc_driver).newInstance();
			// Open a connection
			conn = DriverManager.getConnection(Config.Databases.approved_file_db.db_url, connectProperties);

			String queryString = "select hashOfCert from " + "files" + " where hashOfCert=\"" + certHash + "\" LIMIT 1;";

			stmt = conn.prepareStatement(queryString);
			Logger.debug("QueryString is: {}", stmt.toString());
			ResultSet rs = stmt.executeQuery();
			boolean result;
			if (!rs.isBeforeFirst()) {
				// empty result set (hash not in DB, so we need to add it)
				result = true;
			} else {
				result = false;
			}

			rs.close();
			stmt.close();
			conn.close();
			return result;
		} catch (Exception e) {
			Logger.error(e);
			return false;
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
				Logger.error(se2);
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				Logger.error(se);
			} // end finally try
		}
	}



	/**
	 * This helper function converts the Json objects into ModFile objects which are easier to manipulate in java
	 * 
	 * @param jArr
	 *            - JsonArray object that is a list of JSON objects representing individual files and details about them
	 * @return a list of ModFile objects representing the corresponding Json objects in the original json array
	 */
	static ArrayList<ModFile> convertJsonToPojoList(JsonArray jArr) {
		ArrayList<ModFile> list = new ArrayList<ModFile>();
		Iterator<JsonElement> it = jArr.iterator();
		while (it.hasNext()) {
			list.add(new ModFile(it.next()));
		}
		return list;
	}

	/**
	 * This follows the algorithm of the TPM PCR_extend function to produce a sha1 hash of the files given. If all goes right, the output of this function should match the value of
	 * PCR 19.
	 * 
	 * @param shas
	 *            - String array of hashes of files
	 * @return byte[] of a sha1 hash
	 */
	static byte[] recalculatePCR19(ArrayList<String> shas) {
		byte[] hash = null;
		
		Logger.debug("All shas in list:");
		for(String s : shas){
			Logger.debug(s);
		}
		
		try {
			// Do the first one concatenated to 20 bytes of 0
			int counter = 0;
			Logger.debug("hash " + counter + ": " + shas.get(counter));
			hash = core.Utils.byteSha(core.Utils.concatBytes(new byte[20], core.Utils.hexToBin(shas.get(counter))));
			counter += 1;
			// Continue the algorithm for all hashes in the list
			for (int i = 1; i < shas.size(); i++) {
				Logger.debug("hash " + counter + ": " + shas.get(counter));
				hash = core.Utils.byteSha(core.Utils.concatBytes(hash, core.Utils.hexToBin(shas.get(counter))));
				counter +=1;
			}
			return hash;
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
	}

	// stupid hard coded work around for qemu
	static byte[] recalculatePCR19_qemu(ArrayList<String> shas) {
		byte[] hash = null;
		
		Logger.debug("All shas in list:");
		for(String s : shas){
			Logger.debug(s);
		}
		
		try {
			// Do the first one concatenated to 20 bytes of 0
			int counter = 0;
			byte[] initial = new byte[]{(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff};
			Logger.debug("hash " + counter + ": " + shas.get(counter));
			hash = core.Utils.byteSha(core.Utils.concatBytes(initial, core.Utils.hexToBin(shas.get(counter))));
			counter += 1;
			// Continue the algorithm for all hashes in the list
			for (int i = 1; i < shas.size(); i++) {
				Logger.debug("hash " + counter + ": " + shas.get(counter));
				hash = core.Utils.byteSha(core.Utils.concatBytes(hash, core.Utils.hexToBin(shas.get(counter))));
				counter +=1;
			}
			return hash;
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
	}
	
	/**
	 * This follows the algorithm of SKINIT when calculated the hash of the SLI.
	 * 
	 * @param pathToSableBin
	 *            String of the path to a SABLE binary
	 * @return byte[] of the sha1 hash
	 */
	public static byte[] recalculatePCR17(String pathToSableBin) {
		File f = new File(pathToSableBin);
		try {
			RandomAccessFile raf = new RandomAccessFile(f, "r");

			byte[] len_b = new byte[2];
			raf.seek(4098);
			raf.read(len_b);

			short s = ByteBuffer.wrap(len_b).order(ByteOrder.LITTLE_ENDIAN).getShort();
			int length = s & 0xffff; // converts to unsigned number

			byte[] sli = new byte[length];
			raf.seek(4096);
			raf.readFully(sli);
			raf.close();

			byte[] tohash = Utils.concatBytes(new byte[20], Utils.byteSha(sli));
			return Utils.byteSha(tohash);
		} catch (Exception e) {
			Logger.debug(e);
			return null;
		}
	}

	/**
	 * Description: This is an implementation of tpm-quote-tool's tpm_getpcrhash function in Java. The output of this function is a byte[] made up of the following data:
	 * 
	 * TPM_QUOTE_INFO2: TPM_STRUCTURE_TAG // 0x0036 // 2 bytes "QUT2" // 4 bytes TPM_NONCE // 20 bytes TPM_PCR_INFO_SHORT: TPM_PCR_SELECTION UINT16 sizeOfSelect; // 2 bytes == 0x00
	 * 0x03 SIZEIS(sizeOfSelect) BYTE *pcrSelect; // == 3 byte bit mask TPM_LOCALITY_SELECTION TPM_LOC_ONE (((UINT32)1)<<0) // 1 byte == 0x01 (locality 1) TPM_COMPOSITE_HASH BYTE
	 * digest[TPM_SHA1_160_HASH_LEN]; // 20 bytes
	 * 
	 * 
	 * The total size should -always- be 52 bytes, unless more than 24 PCR's are in play, in which case pcrSelect would be more than 3 bytes as indicated by sizeOfSelect.
	 * 
	 * Note: All variable names mirror those used in the TPM 1.2 structures specification. (and also TrouSerS)
	 * 
	 * @param listOfPcrs
	 *            int[] of the indices of the PCR's used
	 * @param validPcrValues
	 *            ArrayList<byte[]> contains an array of valid PCR values, in order. We get this by calling recalculatePCR19 (for now)
	 * @return byte[] of the recreated output that tpm_getpcrhash would output. It is a "TSS_VALIDATION" structure
	 */
	static byte[] tpmGetPcrHash(int[] listOfPcrs, TreeMap<Integer, byte[]> validPcrValues, String subject) {

		byte[] tpm_structure_tag = new byte[] { (byte) 0x00, (byte) 0x36 };
		byte[] QUT2 = new byte[] { (byte) 0x51, (byte) 0x55, (byte) 0x54, (byte) 0x32 };
		byte[] sizeOfSelect = new byte[] { (byte) 0x00, (byte) 0x03 };
		byte[] tpm_locality_selection = new byte[] { (byte) 0x01 };
		byte[] valueSize;

		// Convert valueSize integer value to 4 bytes, ie. 20 -> 0x00 0x00 0x00 0x14
		valueSize = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(listOfPcrs.length * 20).array();
		byte[] pcrSelect = new byte[3];
		for (int pcr : listOfPcrs) {
			pcrSelect[pcr / 8] |= (1 << (pcr % 8));
		}

		// Create the TPM_PCR_SELECTION structure
		byte[] tpm_pcr_selection = core.Utils.concatBytes(sizeOfSelect, pcrSelect);

		// Create the TPM_PCR_COMPOSITE structure
		byte[] tpm_pcr_composite = core.Utils.concatBytes(tpm_pcr_selection, valueSize);
		for (Integer i : validPcrValues.keySet()) {
			tpm_pcr_composite = core.Utils.concatBytes(tpm_pcr_composite, validPcrValues.get(i));
		}

		// Hash the TPM_PCR_COMPOSITE to make the TPM_COMPOSITE_HASH
		byte[] tpm_composite_hash = core.Utils.byteSha(tpm_pcr_composite);

		// Create the TPM_PCR_INFO_SHORT structure
		byte[] tpm_pcr_info_short = core.Utils.concatBytes(tpm_pcr_selection, tpm_locality_selection, tpm_composite_hash);

		// Call API to get nonce
		byte[] tpm_nonce = core.TpmUtils.getNonce(subject).getBytes();

		// Create the TPM_QUOTE_INFO2 structure
		byte[] tpm_quote_info2 = core.Utils.concatBytes(tpm_structure_tag, QUT2, tpm_nonce, tpm_pcr_info_short);
		return tpm_quote_info2;
	}

	/**
	 * Description: This function will execute the tpm_verifyquote command and return 'true' if the given quote was successfully verified.
	 * 
	 * @param b64quote
	 *            String the base64 of a signed quote from a user
	 * @param sha1uuid
	 *            String of the sha1 of the uuid of the public key for a user
	 * @param validHash
	 *            String the base64 of a valid hash to check against
	 * @return boolean verification success
	 */
	static boolean tpmVerifyQuote(String b64quote, String sha1uuid, String validHash) {
		String pubkPath = core.Config.Tpm.tmp_dir + sha1uuid + ".pubk";

		if (!core.Utils.validFilePath(pubkPath)) { // make sure we have their public key
			Logger.info("Missing public key for quote verification: {}", pubkPath);
			return false;
		}

		try {
			Process pr = Runtime.getRuntime().exec("tpm_verifyquote " + pubkPath + " " + validHash + " " + b64quote);
			pr.waitFor();
			return pr.exitValue() == 0 ? true : false; // if exit code == 0, return true, otherwise return false
		} catch (IOException | InterruptedException e) {
			Logger.error("Failed executing tpm_verifyquote: {}", e);
			return false;
		}
	}

}

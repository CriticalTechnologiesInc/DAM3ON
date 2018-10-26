package helpers;

/*
 * @(#)FilePolicyModule.java
 *
 * Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistribution of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *   2. Redistribution in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in
 * the design, construction, operation or maintenance of any nuclear facility.
 */

import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.MatchResult;
import org.wso2.balana.Policy;
import org.wso2.balana.PolicySet;

import org.wso2.balana.ctx.Status;

import org.wso2.balana.finder.PolicyFinder;
import org.wso2.balana.finder.PolicyFinderModule;
import org.wso2.balana.finder.PolicyFinderResult;

import core.CTIConstants;
import core.Config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


///**
// * This module represents a collection of files containing polices,
// * each of which will be searched through when trying to find a
// * policy that is applicable to a specific request.
// * <p>
// * Note: this module is provided only as an example and for testing
// * purposes. It is not part of the standard, and it should not be
// * relied upon for production systems. In the future, this will likely
// * be moved into a package with other similar example and testing
// * code.
// *
// * @since 1.0
// * @author Seth Proctor
// */

/**
 * This class is a modification of Seth Proctor's FilePolicyModule class. Instead of reading policies from files, we 
 * connect to a database (using a helper class) and read policies in from the DB for use in Request evaluation.
 *
 * @author
 * Justin Fleming - fleminjr@critical.com
 * Jeremy Fields - fieldsjd@critical.com
 *
 */
public class DatabasePolicyModule extends PolicyFinderModule implements ErrorHandler
{
    // the finder that is using this module
//    private PolicyFinder finder; 

    //
    private File schemaFile;

    //
    private Set<String> fileNames;

    //
    private Set<AbstractPolicy> policies;

    // the logger we'll use for all messages
    private static final Logger logger =
        Logger.getLogger(DatabasePolicyModule.class.getName());

    /**
     * Constructor which retrieves the schema file to validate policies against
     * from the POLICY_SCHEMA_PROPERTY. If the retrieved property
     * is null, then no schema validation will occur.
     */
    public DatabasePolicyModule() {
        fileNames = new HashSet<String>();
        policies = new HashSet<AbstractPolicy>();

        String schemaName = System.getProperty(CTIConstants.POLICY_SCHEMA_PROPERTY);

        if (schemaName == null)
            schemaFile = null;
        else
            schemaFile = new File(schemaName);
    }

    /**
     * Constructor that uses the specified input as the schema file to
     * validate policies against. If schema validation is not desired,
     * a null value should be used.
     *
     * @param schemaFile the schema file to validate policies against,
     *                   or null if schema validation is not desired.
     */
    public DatabasePolicyModule(File schemaFile) {
        fileNames = new HashSet<String>();
        policies = new HashSet<AbstractPolicy>();

        this.schemaFile = schemaFile;
    }

    /**
     * Constructor that specifies a set of initial policy files to use.
     * No schema validation is performed.
     *
     * @param fileNames a <code>List</code> of <code>String</code>s that
     *                  identify policy files
     */
    public DatabasePolicyModule(List<String> fileNames) {
        this();

        if (fileNames != null)
            this.fileNames.addAll(fileNames);
    }

    /**
     * Indicates whether this module supports finding policies based on
     * a request (target matching). Since this module does support
     * finding policies based on requests, it returns true.
     *
     * @return true, since finding policies based on requests is supported
     */
    public boolean isRequestSupported() {
        return true;
    }

    /**
     * Initializes the <code>FilePolicyModule</code> by loading
     * the policies contained in the collection of files associated
     * with this module. This method also uses the specified 
     * <code>PolicyFinder</code> to help in instantiating PolicySets.
     *
     * @param finder a PolicyFinder used to help in instantiating PolicySets
     */
    public void init(PolicyFinder finder) {
//        this.finder = finder;

        Iterator<String> it = fileNames.iterator();
        while (it.hasNext()) {
            String fname = (String)(it.next());
            AbstractPolicy policy = loadPolicy(fname, finder,
                                               schemaFile, this);
            if (policy != null)
                policies.add(policy);
        }
    }

    /**
     * Adds a file (containing a policy) to the collection of filenames
     * associated with this module. 
     *
     * @param filename the file to add to this module's collection of files
     */
    public boolean addPolicy(String filename) {
        return fileNames.add(filename);
    }

    /**
     * Loads a policy from the specified filename and uses the specified
     * <code>PolicyFinder</code> to help with instantiating PolicySets.
     *
     * @param filename the file to load the policy from
     * @param finder a PolicyFinder used to help in instantiating PolicySets
     *
     * @return a (potentially schema-validated) policy associated with the 
     *         specified filename, or null if there was an error
     */
    public static AbstractPolicy loadPolicy(String filename,
                                            PolicyFinder finder) {
        return loadPolicy(filename, finder, null, null);
    }

    /**
     * Loads a policy from the specified filename, using the specified
     * <code>PolicyFinder</code> to help with instantiating PolicySets,
     * and using the specified input as the schema file to validate
     * policies against. If schema validation is not desired, a null
     * value should be used for schemaFile
     * 
     * @param filename the file to load the policy from
     * @param finder a PolicyFinder used to help in instantiating PolicySets
     * @param schemaFile the schema file to validate policies against, or
     *                   null if schema validation is not desired
     * @param handler an error handler used to print warnings and errors
     *                during parsing
     *
     * @return a (potentially schema-validated) policy associated with the 
     *         specified filename, or null if there was an error
     */
    public static AbstractPolicy loadPolicy(String filename,
                                            PolicyFinder finder,
                                            File schemaFile,
                                            ErrorHandler handler) {
        try {
            // create the factory
            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);

            DocumentBuilder db = null;

            // as of 1.2, we always are namespace aware
            factory.setNamespaceAware(true);

            // set the factory to work the way the system requires
            if (schemaFile == null) {
                // we're not doing any validation
                factory.setValidating(false);

                db = factory.newDocumentBuilder();
            } else {
                // we're using a validating parser
                factory.setValidating(true);

                factory.setAttribute(CTIConstants.JAXP_SCHEMA_LANGUAGE, CTIConstants.W3C_XML_SCHEMA);
                factory.setAttribute(CTIConstants.JAXP_SCHEMA_SOURCE, schemaFile);
                
                db = factory.newDocumentBuilder();
                db.setErrorHandler(handler);
            }

            /*** Add Database query here ***/
            
            // select POLICY_NAME from TABLE_NAME
            String result = readPolicy(filename); 
            
            InputStream polStream = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
            
            Document doc = db.parse(polStream);
            
            /*******************************/
            
            // handle the policy, if it's a known type
            Element root = doc.getDocumentElement();
            String name = root.getTagName();

            if (name.equals("Policy")) {
                return Policy.getInstance(root);
            } else if (name.equals("PolicySet")) {
                return PolicySet.getInstance(root, finder);
            } else {
                // this isn't a root type that we know how to handle
                throw new Exception("Unknown root document type: " + name);
            }

        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING))
                logger.log(Level.WARNING, "Error reading policy from file " +
                           filename, e);
        }

        // a default fall-through in the case of an error
        return null;
    }

    /**
     * Finds a policy based on a request's context. This may involve using
     * the request data as indexing data to lookup a policy. This will always
     * do a Target match to make sure that the given policy applies. If more
     * than one applicable policy is found, this will return an error.
     * NOTE: this is basically just a subset of the OnlyOneApplicable Policy
     * Combining Alg that skips the evaluation step. See comments in there
     * for details on this algorithm.
     *
     * @param context the representation of the request data
     *
     * @return the result of trying to find an applicable policy
     */
    public PolicyFinderResult findPolicy(EvaluationCtx context) {
        AbstractPolicy selectedPolicy = null;
        Iterator<AbstractPolicy> it = policies.iterator();

        while (it.hasNext()) {
            AbstractPolicy policy = (AbstractPolicy)(it.next());

            // see if we match
            MatchResult match = policy.match(context);
            int result = match.getResult();
            
            // if there was an error, we stop right away
            if (result == MatchResult.INDETERMINATE)
                return new PolicyFinderResult(match.getStatus());

            if (result == MatchResult.MATCH) {
                // if we matched before, this is an error...
                if (selectedPolicy != null) {
                    ArrayList<String> code = new ArrayList<String>();
                    code.add(Status.STATUS_PROCESSING_ERROR);
                    Status status = new Status(code, "too many applicable top-"
                                               + "level policies");
                    return new PolicyFinderResult(status);
                }

                // ...otherwise remember this policy
                selectedPolicy = policy;
            }
        }

        // if we found a policy, return it, otherwise we're N/A
        if (selectedPolicy != null)
            return new PolicyFinderResult(selectedPolicy);
        else
            return new PolicyFinderResult();
    }

    /**
     * Standard handler routine for the XML parsing.
     *
     * @param exception information on what caused the problem
     */
    public void warning(SAXParseException exception) throws SAXException {
        if (logger.isLoggable(Level.WARNING))
            logger.warning("Warning on line " + exception.getLineNumber() +
                           ": " + exception.getMessage());
    }

    /**
     * Standard handler routine for the XML parsing.
     *
     * @param exception information on what caused the problem
     *
     * @throws SAXException always to halt parsing on errors
     */
    public void error(SAXParseException exception) throws SAXException {
        if (logger.isLoggable(Level.WARNING))
            logger.warning("Error on line " + exception.getLineNumber() +
                           ": " + exception.getMessage() + " ... " +
                           "Policy will not be available");

        throw new SAXException("error parsing policy");
    }

    /**
     * Standard handler routine for the XML parsing.
     *
     * @param exception information on what caused the problem
     *
     * @throws SAXException always to halt parsing on errors
     */
    public void fatalError(SAXParseException exception) throws SAXException {
        if (logger.isLoggable(Level.WARNING))
            logger.warning("Fatal error on line " + exception.getLineNumber() +
                           ": " + exception.getMessage() + " ... " +
                           "Policy will not be available");

        throw new SAXException("fatal error parsing policy");
    }
    
	/**
	 * This function when given a subjectID, runs a [MySQL] query against a DB, and
	 * returns a list of results. 
	 * 
	 * @param subjectId String of the 'Subject' we are running the SQL statement on.
	 * An example statement looks like: 
	 * 
	 * ..."select Role from " + TABLE + " where user=?";
	 * ...
	 * stmt = conn.prepareStatement(queryString); 
	 * stmt.setString(1,subjectId)
	 * 
	 * @return ArrayList<String> containing results from [MySQL] statement
	 * @throws IOException
	 */
	public static String readPolicy(String policyName){
		Properties connectProperties = new Properties();
		
		connectProperties.put("user", Config.Databases.PolicyDB.user);
		connectProperties.put("password",Config.Databases.PolicyDB.password);
		/******** SSL ***************/
		connectProperties.put("verifyServerCertificate", "false"); // because we use self-signed certs
		connectProperties.put("useSSL", "true");
		System.setProperty("javax.net.ssl.keyStore", Config.Databases.SSL.keystore_path);
		System.setProperty("javax.net.ssl.keyStorePassword", Config.Databases.SSL.keystore_pw);
		
		/****************************/
		
		
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			// Register JDBC driver mySql
			Class.forName(Config.Databases.PolicyDB.jdbc_driver).newInstance();
			// Open a connection
			conn = DriverManager.getConnection(Config.Databases.PolicyDB.db_url,connectProperties);
			
			String queryString = "select policy from " + Config.Databases.PolicyDB.policy_table +" where name=?";
			
			stmt = conn.prepareStatement(queryString); 
			stmt.setString(1,policyName);
			org.pmw.tinylog.Logger.debug("QueryString is: {}", stmt.toString());
			ResultSet rs = stmt.executeQuery();
			String result = null;
			
			while (rs.next()) {
				result = rs.getString("policy");
			}
			
			rs.close();
			stmt.close();
			conn.close();
			return result;
		} catch (Exception e) {
			org.pmw.tinylog.Logger.error(e);
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
				org.pmw.tinylog.Logger.error(se2);
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				org.pmw.tinylog.Logger.error(se);
			} // end finally try
		}
		return null;
	}
}
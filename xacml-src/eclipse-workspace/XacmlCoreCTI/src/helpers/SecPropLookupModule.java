package helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;

import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.attr.AttributeValue;
import org.wso2.balana.attr.BagAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.finder.AttributeFinderModule;

import core.Config;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
///////////// Potential cause of a problem around line 78-ish //////////////////////////
/**
 * 
 * Extension of Sun's AttributeFinderModule. This module queries a database using the subject-id of the XACML
 *  Request to retrieve associated roles.
 * @author CTI
 *
 */
public class SecPropLookupModule extends AttributeFinderModule {
    private URI defaultSubjectId;

    public SecPropLookupModule() {

        try {
            defaultSubjectId = new URI("hashes");
        } catch (URISyntaxException e) {
           //ignore
        }

    }

    @Override
    public Set<String> getSupportedCategories() {
        Set<String> categories = new HashSet<String>();
        categories.add("urn:oasis:names:tc:xacml:3.0:attribute-category:environment");
        return categories;
    }

    @Override
    public Set<String> getSupportedIds() {
        Set<String> ids = new HashSet<String>();
        ids.add("get-props-from-hashes");
        return ids;   
    }
	
	/** (non-Javadoc)
	 * always return true, since this is a feature we always support
	 * @see com.sun.xacml.finder.AttributeFinderModule#isDesignatorSupported()
	 */
    @Override
	public boolean isDesignatorSupported() {
		return true;
	}


	/** (non-Javadoc)
	 * @see com.sun.xacml.finder.AttributeFinderModule#findAttribute(java.net.URI, java.net.URI, java.net.URI, java.net.URI, com.sun.xacml.EvaluationCtx, int)
	 */
    @Override
	public EvaluationResult findAttribute(URI attributeType, URI attributeId, String issuer, URI environmentCategory,
			EvaluationCtx context) {
		
		
		Logger.info("In SecPropLookupModule:");
		
		EvaluationResult hashes = null;
		String[] hashList = null;
		
		try {
			hashes = context.getAttribute(new URI(StringAttribute.identifier),
					defaultSubjectId, issuer, environmentCategory );
			if (hashes.getAttributeValue().isBag()) {
				BagAttribute attr = (BagAttribute) hashes.getAttributeValue();
				for (Iterator<?> iterator = attr.iterator(); iterator.hasNext();) {
					AttributeValue val = (AttributeValue) iterator.next();
					hashList = val.encode().split(",");
					break;
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		
		return new EvaluationResult(new BagAttribute(attributeType, getSecProps(hashList)));
	}
	
	private List<AttributeValue> getSecProps(String[] hList){		
		String hashList = "(";
		for(String hash: hList){
			hashList+= ("'"+hash+"',");
		}
		// remove trailing comma
		hashList = hashList.substring(0, hashList.length()-1);
		hashList+=")";
		Logger.debug("HashList: {}", hashList);
		
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
			conn = DriverManager.getConnection(Config.Databases.approved_file_db.db_url,connectProperties);
			
			String queryString = "select notBefore,notAfter,secProps,hashOfCert from " + "files" +" where binSha1 in "+ hashList + ";";
			
			stmt = conn.prepareStatement(queryString); 
			Logger.debug("QueryString is: {}", stmt.toString());
			ResultSet rs = stmt.executeQuery();
			
			List<AttributeValue> attributeValues = new ArrayList<AttributeValue>(); 
			while (rs.next()) {
				String[] temp = rs.getString("secProps").split(",");
				String nb = rs.getString("notBefore");
				String na = rs.getString("notAfter");
				
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
				DateTime notBefore = new DateTime(df.parse(nb));
				DateTime notOnOrAfter = new DateTime(df.parse(na));
				Date current = new Date();
				
				if(current.after(notOnOrAfter.toDate()) || current.before(notBefore.toDate())){
					Logger.error("Certificate " + rs.getString("hashOfCert")+" has an invalid date!");
					if(current.after(notOnOrAfter.toDate())){
						removeHashFromDB(rs.getString("hashOfCert"));
					}
					continue;
				}
				
				for(String s: temp){
					attributeValues.add(new StringAttribute(s));
				}
			}
			Logger.debug("Security Property set size:" + attributeValues.size());
			rs.close();
			stmt.close();
			conn.close();
			return attributeValues;
		}catch (Exception e) {
			Logger.error(e);
			return new ArrayList<AttributeValue>();
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
	
	private static boolean removeHashFromDB(String hashOfCert) {
		Connection dbConnection = null;
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
		String queryString = "DELETE FROM " + "files" + " WHERE hashOfCert=\"" + hashOfCert + "\";";
		Statement stmt = null;
		try {
			stmt = dbConnection.createStatement();
			stmt.executeUpdate(queryString);
			return true;
		} catch (SQLException e) {
			Logger.error(e);
			return false;
		} finally {

			if (stmt != null) {
				try {
					stmt.close();
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
}

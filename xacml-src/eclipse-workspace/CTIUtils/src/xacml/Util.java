package xacml;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.Properties;
import org.pmw.tinylog.Logger;
import core.Config;

/**
 * @author 
 * Justin Fleming - fleminjr@critical.com
 * Jeremy Fields - fieldsjd@critical.com
 * 
 *  This is a utilities class that contains helper methods to be
 *  used throughout various classes
 */

public class Util {
	
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
	public static ArrayList<String> readSelectedPoliciesFromDB() {
		
		Properties connectProperties = new Properties();
		String DB_USER = Config.Databases.PolicyDB.user;
		String DB_PASSWORD = Config.Databases.PolicyDB.password;
		connectProperties.put("user", DB_USER);
		connectProperties.put("password", DB_PASSWORD);
		connectProperties.put("useSSL", "true");
		connectProperties.put("verifyServerCertificate", "false"); // because we use self-signed certs
		System.setProperty("javax.net.ssl.keyStore", Config.Databases.SSL.keystore_path);
		System.setProperty("javax.net.ssl.keyStorePassword", Config.Databases.SSL.keystore_pw);
		
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			// Register JDBC driver mySql
			Class.forName(Config.Databases.PolicyDB.jdbc_driver).newInstance();
			// Open a connectionString DB_CONNECTION = Config.Databases.privateNonceDB.db_url;
			
			conn = DriverManager.getConnection(Config.Databases.PolicyDB.db_url,connectProperties);
			
			String queryString = "select * from " + Config.Databases.PolicyDB.selected_policies_table; 
			
			stmt = conn.prepareStatement(queryString); 
			Logger.debug("QueryString is: {}", stmt.toString());
			ResultSet rs = stmt.executeQuery();
			ArrayList<String> selectedPoliciesList = new ArrayList<String>();
			while (rs.next()) {
				selectedPoliciesList.add(rs.getString("name"));
				Logger.debug("Policy Name: {}", rs.getString("name"));
			}
			rs.close();
			stmt.close();
			conn.close();
			return selectedPoliciesList;
			
		} catch (SQLException se) {
			Logger.error(se);
		} catch (Exception e) {
			Logger.error(e);
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
		return null;
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
	public static ArrayList<String> readRoles(String subjectId) {
		Properties connectProperties = new Properties();
		
		connectProperties.put("user", Config.Databases.RoleDB.user);
		connectProperties.put("password", Config.Databases.RoleDB.password);
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			// Register JDBC driver mySql
			Class.forName(Config.Databases.RoleDB.jdbc_driver).newInstance();
			// Open a connection
			conn = DriverManager.getConnection(Config.Databases.RoleDB.db_url,connectProperties);
			
			String queryString = "select role from " + Config.Databases.RoleDB.table + " where user=?";
			Logger.debug("QueryString is: {}", queryString);
			stmt = conn.prepareStatement(queryString); 
			stmt.setString(1,subjectId);
			ResultSet rs = stmt.executeQuery();
			ArrayList<String> roleList = new ArrayList<String>();
			while (rs.next()) {
				roleList.add(rs.getString("role"));
				Logger.debug("Adding Role: {}", rs.getString("role"));
			}
			rs.close();
			stmt.close();
			conn.close();
			return roleList;
		} catch (Exception e) {
			Logger.error(e);
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
		return null;
	}

	

	

}

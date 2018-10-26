package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.pmw.tinylog.Logger;


public class TpmUtils {

	/**
	 * 
	 * Description: Removes the public nonce from the nonce database after it's been used
	 * 
	 * @param email String of the users email address
	 * @return boolean success
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static boolean deletePubNonce(String email){
		String DB_CONNECTION = Config.Databases.publicNonceDB.db_url;
		String DB_USER = Config.Databases.publicNonceDB.user;
		String DB_PASSWORD = Config.Databases.publicNonceDB.password;
		Connection dbConnection = null;
		try {
			Class.forName(Config.Databases.publicNonceDB.jdbc_driver).newInstance();
			
			/** NEW ******************************************************************/
			Properties connectProperties = new Properties();
			connectProperties.put("user", DB_USER);
			connectProperties.put("password", DB_PASSWORD);
			connectProperties.put("useSSL", "true");
			connectProperties.put("verifyServerCertificate", "false"); // because we use self-signed certs
			System.setProperty("javax.net.ssl.keyStore", Config.Databases.SSL.keystore_path);
			System.setProperty("javax.net.ssl.keyStorePassword", Config.Databases.SSL.keystore_pw);

			dbConnection = DriverManager.getConnection(DB_CONNECTION, connectProperties);
			
			/*************************************************************************/
			
			String query = "delete from nonce_table where email = ?";
			PreparedStatement preparedStmt = dbConnection.prepareStatement(query);
			preparedStmt.setString(1, email);
			preparedStmt.execute();
			dbConnection.close();
			return true;
		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			Logger.error("Database connection error in TpmUtils." + e.getMessage());
			return false;
		}
	}
	
	/**
	 * Description: This calls API for public nonce and returns it.
	 * 
	 * @param email String email address of user to get nonce for
	 * @return nonce as String
	 * @throws IOException
	 */
	public static String getNonce(String email){
		StringBuilder result = new StringBuilder();
		try{
			URL url = new URL(Config.MultiLoginPep.pub_nonce_api + email);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			rd.close();
		} catch(Exception e){
			Logger.error("Error in TpmUtils.getNonce: {}", e.toString());
			e.printStackTrace(); // TODO remove this
			System.exit(2); // TODO remove this
			return "";
		}
		JsonElement jElem = new JsonParser().parse(result.toString());
		JsonObject jObj = jElem.getAsJsonObject();

		return jObj.get("nonce").getAsString();
	}
}

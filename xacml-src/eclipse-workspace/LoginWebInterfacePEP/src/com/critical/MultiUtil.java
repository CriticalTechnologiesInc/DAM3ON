package com.critical;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.opensaml.saml2.core.Response;
import org.pmw.tinylog.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import core.CTIConstants;
import core.Config;
import core.PGPHelper;
import core.Utils;
import helpers.EXIDecoder;
import helpers.EXIEncoder;
import helpers.OpenSamlHelper;
import xacml.RequestMaker;
import xacml.RequestMaker.Tuple;

/**
 * 
 * @author Justin Fleming - fleminjr@critical.com Jeremy Fields - fieldsjd@critical.com
 *
 * 
 *         Description: Utility class for MultiLogin.java
 *
 */
public class MultiUtil {

	private static final String repo_dir = "/opt/git/";

	/**
	 * Description: Calls PDP API to evaluate an XACML requests
	 * 
	 * @param request
	 *            XACML request as String
	 * @return response XACML response as String
	 */
	public static String evaluateRequest(String request) {
		HttpClient httpClient = HttpClientBuilder.create().build();

		HttpPost httpPost = new HttpPost(Config.MultiLoginPep.web_pdp_url);
		// Request parameters and other properties.
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("SAMLRequest", request));

		StringBuffer postResponse = null;
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, "US-ASCII"));
			HttpResponse response = httpClient.execute(httpPost);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			postResponse = new StringBuffer();
			String line2 = "";
			while ((line2 = rd.readLine()) != null) {
				postResponse.append(line2);
			}
		} catch (IOException e) {
			Logger.debug(e);
		}

		return postResponse.toString();
	}

	public static String checkHashApi(String json) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(Config.MultiLoginPep.hashcheck_url);
		// Request parameters and other properties.
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("filesList", json));

		StringBuffer postResponse = null;
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, "US-ASCII"));
			HttpResponse response = httpClient.execute(httpPost);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			postResponse = new StringBuffer();
			String line2 = "";
			while ((line2 = rd.readLine()) != null) {
				postResponse.append(line2);
			}
		} catch (IOException e) {
			Logger.debug(e);
			return null;
		}
		return postResponse.toString();
	}

	/**
	 * Description: EXI/B64 decodes an XACML response
	 * 
	 * @param response
	 *            EXI/B64 encoded XACML
	 * @return plain text XACML response
	 */
	public static String decodeXACMLResponse(String response) {
		Logger.debug("raw PDP response: {}", response);
		JsonParser parser = new JsonParser();
		JsonObject jObject = null;
		try{
			jObject = parser.parse(response).getAsJsonObject();
		}catch(IllegalStateException ise){
			core.TinyLogConfig.killTinyLog();
			throw new IllegalStateException(ise);
		}

		JsonElement jel = jObject.get("SAMLResponse");
		String atLast = jel.toString();
		String decodedResponse = "";

		if (!atLast.isEmpty()) {
			byte[] binaryEXI = Base64.getMimeDecoder().decode(atLast);
			decodedResponse = new EXIDecoder().decodeEXIDefault(binaryEXI);
		}
		return decodedResponse;
	}

	/**
	 * Description: This method generates the random string. This is used to create the private nonce for the login system.
	 * 
	 * @return String that is a 25 character long random sequence of uppercase letters, and 0-9.
	 */
	public static String generateNonce() {
		final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		int count = 15;
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		int unixtime = (int) (System.currentTimeMillis() / 1000L);
		String nonce = builder.toString();
		nonce += Integer.toString(unixtime);
		return nonce;
	}

	/**
	 * 
	 * Description: Creates a basic XACML request.
	 * 
	 * @param subject
	 *            String representing the subject
	 * @param action
	 *            String representing the action
	 * @param resource
	 *            String representing the resource
	 * @return request String of XACML request
	 * @throws IOException
	 */
	public static String formBasicSARRequest(String subject, String action, String resource, List<Tuple<String, ?>> envs) {
		Logger.debug("call to formBasicSARRequest");
		Logger.info("subject={}, resource={}, action={}", subject, resource, action);

		/************************** new *************************/
		ArrayList<String> combos = new ArrayList<String>();
		String cap = "";
		String resource_s = "";
		boolean res_is_f = resource_is_file(resource);
		if (res_is_f) {
			// file://radio_repo/a/b/c.txt

			// copy argument 'resource' to 'resource_s' so that we can keep existing code still using 'resource'
			resource_s = resource;

			// Strip string of 'file://' and split on '/' to get length of path to file
			String[] split = resource_s.substring(7).split("/");

			// Repo name is 0th element of split
			resource = split[0]; // i.e. Radio_repo

			// Remove repo name to be left with a path within the repo
			// ie. 'file://radio_repo/a/b/c.txt' becomes '/a/b/c.txt'
			String temp = resource_s.substring(7);
			temp = temp.substring(temp.indexOf("/"));

			// save full repo_path for later use
			String repo_path = temp;

			// Check if file exists - exit if it doesn't. that means user requested an invalid resource
			String full_local_path = repo_dir + resource + repo_path;
			if (!fileOrSymLinkExists(full_local_path)) {
				Logger.error("User requested a file that is not available on the local file system: " + full_local_path);
				return null;
			}

			// add all subpaths within repo_path to list
			for (int i = 0; i < split.length - 1; i++) {
				combos.add(temp);
				temp = Paths.get(temp).getParent().toString();
			}
			// add root of repo to list as well
			combos.add("/");

			cap = getCapability(resource, repo_path);

			if (!isCapEncrypted(cap)) {
				Logger.debug("CAP wasn't encrypted - encrypting now!");
				try {
					cap = Utils.encodeBase64(PGPHelper.encryptString(cap.getBytes(), Config.Pgp.pdp_pubkey, null, true));
					Logger.debug("B64 encoded cap: {}", cap);
				} catch (Exception e) {
					Logger.error(e);
				}
				if (!replaceCapability(resource, repo_path, cap)) {
					Logger.error("Problem replacing capability");
					return null;
				}
			}
		}

		/********************************************************/

		RequestMaker r = new RequestMaker();
		List<Tuple<String, ?>> subs = new ArrayList<Tuple<String, ?>>();
		subs.add(r.new Tuple<String, String>(CTIConstants.SUBJECT_ATTRIBUTEID, subject));
		List<Tuple<String, ?>> acts = new ArrayList<Tuple<String, ?>>();
		acts.add(r.new Tuple<String, String>(CTIConstants.ACTION_ATTRIBUTEID, action));
		List<Tuple<String, ?>> ress = new ArrayList<Tuple<String, ?>>();
		ress.add(r.new Tuple<String, String>(CTIConstants.RESOURCE_ATTRIBUTEID, resource));

		/************************* new **************************/
		if ((combos.size() != 0) && res_is_f) {
			for (int i = 0; i < combos.size(); i++) {
				ress.add(r.new Tuple<String, String>("multi-resource", combos.get(i)));
			}
			ress.add(r.new Tuple<String, String>("full-path", resource_s));
			ress.add(r.new Tuple<String, String>("token", cap));
		}
		/********************************************************/

		String requestString = r.createRequest(subs, acts, ress, envs);
		OpenSamlHelper osh = new OpenSamlHelper();
		String signed_request = osh.wrapAndSignRequest(requestString);
	
		byte[] ba_request = new EXIEncoder().encodeEXIDefault(signed_request);
		String encoded_signed_request_b64 = Base64.getMimeEncoder().encodeToString(ba_request).replaceAll("\r", "").replaceAll("\n", "");

		return encoded_signed_request_b64;
	}

	public static boolean isCapEncrypted(String cap) {
		return Utils.isStringB64Encoded(cap) ? Utils.decodeBase64(cap).contains("BEGIN PGP MESSAGE") : false;
	}

	/**
	 * Description: This method will find the capability of a file, and replace it with a new capability. This is typically used to swap between encrypted and decrypted
	 * capabilities.
	 * 
	 * @param resource
	 *            String of the repository name (i.e. Radio_repo)
	 * @param repo_path
	 *            String of the path to the file within the repo, whose capability is going to be replaced
	 * @param newCap
	 *            String of the new capability that will replace the existing one
	 * @return boolean success
	 */
	private static boolean replaceCapability(String repo_name, String repo_path, String newCap) {

		//////
		String output = null;
		try {
			Process p = Runtime.getRuntime().exec("/etc/webxacml/tahoepep/tahoepepWrapper.py repcap " + repo_name + "/" + repo_path + " " + newCap);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			// do I have to wait..?
			output = stdInput.readLine();
			Logger.debug("New repCap function returned: " + output);

			return output.equals("success") ? true : false;
		} catch (Exception e) {
			Logger.error(e);
			return false;
		}
		//////////
		//
		// Repository repo = null;
		// String key_loc = "";
		// String internal_repo_dir = "/opt/git/";
		// try {
		// repo = new FileRepositoryBuilder().setGitDir(new File(internal_repo_dir + resource + "/.git")).build();
		// } catch (Exception e) {
		// Logger.error(e);
		// }
		//
		// // Create Git object
		// Git git = new Git(repo);
		// try {
		// if (repo.getBranch().equals("master")) {
		// key_loc = getKeylocation(resource, repo_path, internal_repo_dir);
		// // If we're not in the git-annex branch, switch to it
		// try {
		// Logger.debug("Switching to the git-annex branch");
		// git.checkout().setName("git-annex").call();
		// } catch (GitAPIException e) {
		// Logger.error(e);
		// }
		// } else {
		// try {
		// Logger.debug("switching to the git-annex branch");
		// git.checkout().setName("master").call();
		// key_loc = getKeylocation(resource, repo_path, internal_repo_dir);
		// git.checkout().setName("git-annex").call();
		// } catch (GitAPIException e) {
		// Logger.error(e);
		// }
		// }
		// } catch (Exception e) {
		// Logger.error(e);
		// }
		//
		// System.out.println("Just got key location: " + key_loc);
		// try {
		// System.out.println("Current branch: " + repo.getBranch());
		// } catch (IOException e) {
		// Logger.error(e);
		// }
		//
		// String key_contents = readFile(key_loc);
		//
		// String[] sa = key_contents.split(" ");
		// sa[sa.length - 1] = newCap;
		//
		// String newFileContents = "";
		// for (String part: sa){
		// newFileContents = newFileContents + part + " ";
		// }
		//
		// newFileContents = newFileContents.trim();
		//
		// //write file
		// try {
		// Utils.writeFile(key_loc, newFileContents);
		// } catch (Exception e) {
		// Logger.error(e);
		// git.close();
		// return false;
		// }
		//
		// try {
		// Logger.debug("Switching to the master branch");
		// git.add().addFilepattern(".").call();
		// git.commit().setMessage("automated").call();
		// git.checkout().setName("master").call();
		// } catch (Exception e) {
		// Logger.error(e);
		// git.close();
		// return false;
		// }
		// git.close();
		// return true;
	}

	/**
	 * 
	 * Description: Inserts a private nonce into the pnonce DB to be used to set session data for a user after getting authorization for a resource.
	 * 
	 * @param sub
	 *            String subject
	 * @param act
	 *            String action
	 * @param res
	 *            String resource
	 * @param dec
	 *            String decision
	 * @param url
	 *            String resource URL
	 * @param nonce
	 *            String of private nonce
	 * @param additionUrls
	 *            String of additional resource URL user is granted access to
	 * @return boolean for success
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static boolean insertPrivateNonceDB(String sub, String act, String res, String dec, String url, String nonce, String additionUrls, String cap,
			String accessToken, String arg) {

		String DB_CONNECTION = Config.Databases.privateNonceDB.db_url;
		String DB_USER = Config.Databases.privateNonceDB.user;
		String DB_PASSWORD = Config.Databases.privateNonceDB.password;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;

		// Somehow, since this is called after removePnonce, the SSL stuff isn't required since it's already been set.
		// I understand the System.setProperty stuff, but it doesn't need useSSL or verifyServerCertificate either
		// I guess it somehow remembers (or maybe the DB remembers). However, it's included in the event this function was ever
		// called on it's own.
		Properties connectProperties = new Properties();
		connectProperties.put("user", DB_USER);
		connectProperties.put("password", DB_PASSWORD);
		connectProperties.put("useSSL", "true");
		connectProperties.put("verifyServerCertificate", "false"); // because we use self-signed certs
		System.setProperty("javax.net.ssl.keyStore", Config.Databases.SSL.keystore_path);
		System.setProperty("javax.net.ssl.keyStorePassword", Config.Databases.SSL.keystore_pw);

		try {
			Class.forName(Config.Databases.privateNonceDB.jdbc_driver).newInstance();
			dbConnection = DriverManager.getConnection(DB_CONNECTION, connectProperties);
		} catch (Exception e) {
			Logger.error(e);
			return false;
		}

		String insertTableSQL = "INSERT INTO pnonce_table" + "(subject, action, resource, decision, url, nonce, url_json, cap, atoken, arg) VALUES" + "(?,?,?,?,?,?,?,?,?,?)";
		try {
			preparedStatement = dbConnection.prepareStatement(insertTableSQL);
			preparedStatement.setString(1, sub);
			preparedStatement.setString(2, act);
			preparedStatement.setString(3, res);
			preparedStatement.setString(4, dec);
			preparedStatement.setString(5, url);
			preparedStatement.setString(6, nonce);
			preparedStatement.setString(7, additionUrls);
			preparedStatement.setString(8, cap);
			preparedStatement.setString(9, accessToken);
			preparedStatement.setString(10, arg);
			preparedStatement.executeUpdate();
			Logger.debug("Successfully inserted private nonce into DB");
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
				} catch (SQLException e) {
					Logger.error("Database error: " + e);
				}
			}
		}
	}

	/**
	 * 
	 * Description: Removes the private nonce from the pnonce database after it's been used
	 * 
	 * @param email
	 *            String of email address of user
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static void removePnonce(String email) {
		String DB_CONNECTION = Config.Databases.privateNonceDB.db_url;
		String DB_USER = Config.Databases.privateNonceDB.user;
		String DB_PASSWORD = Config.Databases.privateNonceDB.password;
		Connection dbConnection = null;
		Properties connectProperties = new Properties();
		connectProperties.put("user", DB_USER);
		connectProperties.put("password", DB_PASSWORD);
		connectProperties.put("useSSL", "true");
		connectProperties.put("verifyServerCertificate", "false"); // because we use self-signed certs
		System.setProperty("javax.net.ssl.keyStore", Config.Databases.SSL.keystore_path);
		System.setProperty("javax.net.ssl.keyStorePassword", Config.Databases.SSL.keystore_pw);

		try {
			Class.forName(Config.Databases.privateNonceDB.jdbc_driver).newInstance();
			dbConnection = DriverManager.getConnection(DB_CONNECTION, connectProperties);
			String query = "delete from pnonce_table where subject = ?";
			PreparedStatement preparedStmt = dbConnection.prepareStatement(query);
			preparedStmt.setString(1, email);
			preparedStmt.execute();
			dbConnection.close();
		} catch (Exception e) {
			Logger.debug("Database error: " + e);
		}

	}

	
	/**
	 * 
	 * Description: Creates an XACML attestation request
	 * 
	 * @param subject
	 *            String subject
	 * @param action
	 *            String action
	 * @param resource
	 *            String resource
	 * @param quote
	 *            String TPM quote
	 * @param uuid
	 *            String tpm UUID
	 * @return String of encoded XACML request
	 * @throws IOException
	 */
	public static String formAttestRequest(String subject, String action, String resource, List<Tuple<String, ?>> envs, JsonObject jObj) {
		Logger.info("POST request at {}", new Timestamp(new Date().getTime()));
		Logger.info("subject={}, resource={}, action={}", subject, resource, action);

		/************************** new *************************/
		ArrayList<String> combos = new ArrayList<String>();
		String cap = "";
		String resource_s = "";
		boolean res_is_f = resource_is_file(resource);
		if (res_is_f) {
			// file://radio_repo/a/b/c.txt

			// copy argument 'resource' to 'resource_s' so that we can keep existing code still using 'resource'
			resource_s = resource;

			// Strip string of 'file://' and split on '/' to get length of path to file
			String[] split = resource_s.substring(7).split("/");

			// Repo name is 0th element of split
			resource = split[0];

			// Remove repo name to be left with a path within the repo
			// ie. 'file://radio_repo/a/b/c.txt' becomes '/a/b/c.txt'
			String temp = resource_s.substring(7);
			temp = temp.substring(temp.indexOf("/"));

			// save full repo_path for later use
			String repo_path = temp;

			// Check if file exists - exit if it doesn't. that means user requested an invalid resource
			String full_local_path = repo_dir + resource + repo_path;
			if (!fileOrSymLinkExists(full_local_path))
				return null;

			// add all subpaths within repo_path to list
			for (int i = 0; i < split.length - 1; i++) {
				combos.add(temp);
				temp = Paths.get(temp).getParent().toString();
			}
			// add root of repo to list as well
			combos.add("/");

			cap = getCapability(resource, repo_path);
		}
		/********************************************************/

		RequestMaker r = new RequestMaker();
		List<Tuple<String, ?>> subs = new ArrayList<Tuple<String, ?>>();
		subs.add(r.new Tuple<String, String>(CTIConstants.SUBJECT_ATTRIBUTEID, subject));

		List<Tuple<String, ?>> acts = new ArrayList<Tuple<String, ?>>();
		acts.add(r.new Tuple<String, String>(CTIConstants.ACTION_ATTRIBUTEID, action));

		List<Tuple<String, ?>> ress = new ArrayList<Tuple<String, ?>>();
		ress.add(r.new Tuple<String, String>(CTIConstants.RESOURCE_ATTRIBUTEID, resource));

		JsonArray jArr = jObj.get("hashList").getAsJsonArray();
		Iterator<JsonElement> it = jArr.iterator();

		String tmp = "";
		while (it.hasNext()) {
			tmp += it.next().getAsString();
			if (it.hasNext()) {
				tmp += ",";
			}
		}

		envs.add(r.new Tuple<String, String>("hashes", tmp));

		/************************* new **************************/
		if ((combos.size() != 0) && res_is_f) {
			for (int i = 0; i < combos.size(); i++) {
				ress.add(r.new Tuple<String, String>("multi-resource", combos.get(i)));
			}
			ress.add(r.new Tuple<String, String>("full-path", resource_s));
			ress.add(r.new Tuple<String, String>("token", cap));
		}
		/********************************************************/

		String requestString = r.createRequest(subs, acts, ress, envs);
		
		OpenSamlHelper osh = new OpenSamlHelper();
		String signed_request = osh.wrapAndSignRequest(requestString);
	
		byte[] ba_request = new EXIEncoder().encodeEXIDefault(signed_request);
		String encoded_signed_request_b64 = Base64.getMimeEncoder().encodeToString(ba_request).replaceAll("\r", "").replaceAll("\n", "");
		
		core.TinyLogConfig.killTinyLog();
		return encoded_signed_request_b64;
		
	}

	public static String canPath(String givenPath) {
		File file = new File("/" + givenPath);
		String pre = "/opt/git";
		String can = "";
		try {
			can = file.getCanonicalPath();
		} catch (Exception e) {
			Logger.error(e);
		}
		return pre + can;
	}

	
	/**
	 * Description: Checks what advice are returned. Should be: [auth, attest, both, none].
	 * 
	 * @param response
	 *            the XACML response containing advice
	 * @return String representing what security is required to login
	 */
	public static boolean getBoolAdvice(String response, String attributeid) {
		List<String> attest_t = xacml.XacmlParser.getAdvice(response, attributeid);
		String attest = (attest_t == null) ? "false" : attest_t.get(0);

		if (attest.equals("true")) 
			return true;
		return false;

	}
	
	
	public static String addTokenToDb(String email, String res) {
		long ms = System.currentTimeMillis();
		String token = core.Utils.generateRandomString(20);

		String DB_CONNECTION = Config.Databases.tokenDB.db_url;
		String DB_USER = Config.Databases.tokenDB.user;
		String DB_PASSWORD = Config.Databases.tokenDB.password;
		Connection dbConnection = null;
		PreparedStatement preparedStatement = null;

		// Somehow, since this is called after removePnonce, the SSL stuff isn't required since it's already been set.
		// I understand the System.setProperty stuff, but it doesn't need useSSL or verifyServerCertificate either
		// I guess it somehow remembers (or maybe the DB remembers). However, it's included in the event this function was ever
		// called on it's own.
		Properties connectProperties = new Properties();
		connectProperties.put("user", DB_USER);
		connectProperties.put("password", DB_PASSWORD);
		connectProperties.put("useSSL", "true");
		connectProperties.put("verifyServerCertificate", "false"); // because we use self-signed certs

		System.setProperty("javax.net.ssl.keyStore", Config.Databases.SSL.keystore_path);
		System.setProperty("javax.net.ssl.keyStorePassword", Config.Databases.SSL.keystore_pw);

		try {
			Class.forName(Config.Databases.privateNonceDB.jdbc_driver).newInstance();
			dbConnection = DriverManager.getConnection(DB_CONNECTION, connectProperties);
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}

		String queryStringTI = "select time,token from tokens where user=?";
		PreparedStatement stmt = null;
		ArrayList<String> times = new ArrayList<String>();
		ArrayList<String> tokens = new ArrayList<String>();
		
		try {
			stmt = dbConnection.prepareStatement(queryStringTI);
			stmt.setString(1, email);
			Logger.debug("QueryString is: {}", stmt.toString());
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				times.add(rs.getString("time"));
				tokens.add(rs.getString("token"));
			}

			rs.close();
			stmt.close();
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
		
		
		for(int i = 0; i<times.size(); i++){
			String time = times.get(i);
			if((System.currentTimeMillis() - Long.parseLong(time)) > 300000){
				String query = "delete from tokens where token = ?";
				PreparedStatement preparedStmt;
				try {
					preparedStmt = dbConnection.prepareStatement(query);
					preparedStmt.setString(1, tokens.get(i));
					preparedStmt.execute();
				} catch (SQLException e) {
					Logger.error(e);
				}
				
			}
		}
		

		String insertTableSQL = "INSERT INTO tokens" + "(token, user, time, res) VALUES" + "(?,?,?,?)";
		try {
			preparedStatement = dbConnection.prepareStatement(insertTableSQL);
			preparedStatement.setString(1, token);
			preparedStatement.setString(2, email);
			preparedStatement.setString(3, Long.toString(ms));
			preparedStatement.setString(4, res);
			preparedStatement.executeUpdate();
			Logger.debug("Successfully inserted private nonce into DB");
			return token;
		} catch (SQLException e) {
			Logger.error(e);
			return null;
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
				} catch (SQLException e) {
					Logger.error("Database error: " + e);
				}
			}
		}

	}

	
	public static boolean accessTokenAdvicePresent(String response) {
		return xacml.XacmlParser.getAdvice(response, "need-token") == null ? false : true;
	}

	private static boolean resource_is_file(String res) {
		try {
			if (res.substring(0, 7).toLowerCase().equals("file://")) {
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	private static String getCapability(String repo_name, String repo_path) {
		//////
		String cap = null;
		try {
			Process p = Runtime.getRuntime().exec("/etc/webxacml/tahoepep/tahoepepWrapper.py getcap " + repo_name + "/" + repo_path);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			cap = stdInput.readLine();
			Logger.debug("New getCap function returned: " + cap);
			return cap;
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
	}

	public static String getKeylocation(String resource, String repo_path, String internal_repo_dir) {
		File f = new File(internal_repo_dir + resource + "/" + repo_path);

		if (Files.isSymbolicLink(f.toPath())) {
			String key = readSymLink(f).getFileName().toString();
			String key_md5 = md5(key);
			return internal_repo_dir + resource + "/" + key_md5.substring(0, 3) + "/" + key_md5.substring(3, 6) + "/" + key + ".log.rmt";
		} else {
			return null;
		}
	}

	public static String readFile(String path) {
		byte[] encoded = null;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
			return new String(encoded, StandardCharsets.UTF_8);
		} catch (IOException e) {
			return null;
		}
	}

	public static Path readSymLink(File f) {
		try {
			return Files.readSymbolicLink(f.toPath());
		} catch (IOException x) {
			System.err.println(x);
			return null;
		}
	}

	public static String md5(String input) {
		byte[] inputBytes = input.getBytes();
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// there is such an algorithm, so ignoring.
		}
		byte[] digest = md.digest(inputBytes);
		return byteArrayToHex(digest);

	}

	public static String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for (byte b : a)
			sb.append(String.format("%02x", b));
		return sb.toString();
	}

	public static boolean fileOrSymLinkExists(String path) {
		return Files.isSymbolicLink(new File(path).toPath()) || new File(path).exists();
	}

	

	public static String extractDecryptedCap(String decoded_response) {
		xacml.SignatureHelper helper = new xacml.SignatureHelper();
		Response resObject = (Response) helper.unmarshall(decoded_response);

		// TODO what in tarnation is this
		String decryptedToken = resObject.getAssertions().get(0).getAttributeStatements().get(0).getAttributes().iterator().next().getAttributeValues().get(0).getDOM()
				.getTextContent();
		Logger.info("Extracted the decrypted token: {}", decryptedToken);
		decryptedToken = core.Utils.decodeBase64(decryptedToken);
		return decryptedToken.trim();
	}

}

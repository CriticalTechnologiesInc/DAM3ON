package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.pmw.tinylog.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Jeremy Fields - fieldsjd@critical.com
 * This class exists because there are no feasible alternatives to performing this action.
 * There are no good, usable, stable Java libraries for interacting with GPG when this was written.
 * This class contains all methods needed for verifying the signature on a GPG signed message.
 */
public class GPGHelperCLI {
	/**
	 * This is the higher level, public function for verifying a GPG signed message.
	 * It takes in an email address and a signed message, and returns true if the message has a signature
	 * tied to public key associated with the given email address. False if otherwise.
	 * 
	 * @param email String of the users email address
	 * @param signature String of a GPG signed message
	 * @return boolean whether the signed message's signature is verified
	 * @throws IOException
	 */
	public static boolean verifySignature(String email, String signature) {

		String keyPath = Config.Pgp.pgp_tmp_dir + email + ".asc";
		String sigPath = Config.Pgp.pgp_tmp_dir + email + System.currentTimeMillis() + "sig.txt";

		boolean inKeyRing = false;
		if (!isKeyInKeyring(email)) { // If we don't already have the key..

			try {
				
				String pubkey = null;
				
				try{
					pubkey = getPubKey(email);
				}catch (Exception e){
					pubkey = getPubKey(email, 0);
				}
				
				if((pubkey == null) || pubkey.isEmpty()){ 
					return false;
				}else{ // if we got a pubkey. Note: Order matters! can't check isEmpty if it's null
					if(core.Utils.writeFile(keyPath, pubkey)){ // Get it, and write it to a file
						inKeyRing = runGpg("import", keyPath); // Then call GPG to import it
						core.Utils.deleteFile(keyPath); // Then remove the file
					}else{
						return false;
					}
						
				}
			} catch (Exception e) {
				Logger.error(e);
				return false;
			}
		} else {
			inKeyRing = true; // Otherwise signal that we already have it
		}

		try{
			if (inKeyRing && core.Utils.writeFile(sigPath, signature)) { //If key in keyring and we successfully wrote the signed message to
				// a file
				boolean result = runGpg("verify", sigPath); // verify the signature
				deleteKey(email);
				core.Utils.deleteFile(sigPath); // remove the signature file
				return result; // return the result
			} else {
				return false;
			}
		} catch (Exception e) {
			Logger.error(e);
			return false;
		}
	}

	/**
	 * This is the higher level, public function for verifying a GPG signed message.
	 * It takes in a signed message, and returns true if the message's signature is verified
	 * The public key of the user must already be in the keyring.
	 * 
	 * @param signature String of a GPG signed message
	 * @return boolean whether the signed message's signature is verified
	 * @throws IOException
	 */
	public static boolean verifySignature(String signature) {

		String sigPath = Config.Pgp.pgp_tmp_dir + System.currentTimeMillis() + "sig.txt";
//		String sigPath = "c:\\users\\jeremy\\desktop\\" + System.currentTimeMillis() + "sig.txt";
		try{
			if (core.Utils.writeFile(sigPath, signature)) { //If key in keyring and we successfully wrote the signed message to
				// a file
				boolean result = runGpg("verify", sigPath); // verify the signature
				core.Utils.deleteFile(sigPath); // remove the signature file
				return result; // return the result
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e);
			return false;
		}
	}
	
	/**
	 * This function is similar to the runGpg function, except this needs to parse the output.
	 * Specifically, this checks if the input email address is present in the output of the
	 * "gpg --list-keys" command. 
	 * 
	 * It's purpose is to check if given an email address, if there's an associated key in the gpg keyring.
	 * This isn't the best way for checking if a key exists in a keyring, but there are no feasible alternatives.
	 * Albeit naive to say, this should be pretty robust in the context of this application. 
	 * 
	 * @param email String of a users email address
	 * @return boolean Whether there exists a key in the keyring matching the given email address
	 */
	private static boolean isKeyInKeyring(String email) {
		Runtime rt = Runtime.getRuntime();
		StringBuilder processOutput = new StringBuilder();
		
		try {
			Process pr = rt.exec("gpg --list-keys");
			pr.waitFor();
			try (BufferedReader processOutputReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));) {
				String readLine;
				while ((readLine = processOutputReader.readLine()) != null) {
					processOutput.append(readLine + System.lineSeparator());
				}
				pr.waitFor();

				if (!(pr.exitValue() == 0))
					return false;
			}
		} catch (Exception e) {
			Logger.error(e);
			return false;
		}

		Matcher m = Pattern.compile("(?m)^.*$").matcher(processOutput.toString().toLowerCase());

		while (m.find()) {
			if (m.group().contains(email.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This is a generic function for running a gpg command in the format: gpg --COMMAND ARGUMENT
	 * 
	 * @param command String The gpg command to be ran
	 * @param arg String the argument to be given with that command
	 * @return int exit code of the run
	 */
	private static boolean runGpg(String command, String arg) {
		Runtime rt = Runtime.getRuntime();
		try {
			Process pr = rt.exec("gpg --" + command + " " + arg);
			pr.waitFor();

			if (pr.exitValue() == 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Logger.error(e);
			return false;
		}
	}

	/**
	 * Description:
	 * 
	 * This removes a public key from a keyring based on the associated email address
	 * 
	 * @param email String of email address
	 * @return boolean success
	 */
	private static boolean deleteKey(String email) {
	
		Runtime rt = Runtime.getRuntime();
		try {
			Process pr = rt.exec("gpg --batch --yes --delete-key " + email);
			pr.waitFor();

			if (pr.exitValue() == 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Logger.error(e);
			return false;
		}
		
	}
	
	/**
	 * This function takes in an email and an attempt number. This function will make
	 * an HTTP GET request with a timeout of 5 seconds to a HKP compliant PGP key server.
	 * 
	 * If successful, it will parse the HTML (XML) to find the key, extract it, and return it.
	 * 
	 * Unfortunately, key servers fail/timeout at an abnormally high rate, so if the connection fails for any reason,
	 * we recursively call the function and increment the "attempt" argument.
	 * 
	 * The "attempt" argument is used to specify which key-server in this servers configuration to use.
	 * 
	 * @param email String of the email address to get the key of
	 * @param attempt int of the attempt number.
	 * @return String of the public key, or null if one can't be found and/or all key-servers fail
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	private static String getPubKey(String email, int attempt) {
		String urlToRead;
		
		// make sure we have another key_server
		if(attempt < Config.Pgp.key_servers.size())
			urlToRead = Config.Pgp.key_servers.get(attempt) + email.replace("@", "%40");
		else
			return null;

		StringBuilder html = new StringBuilder();
		URL url = null;
		BufferedReader rd;
		String line;
		// Attempt connection to server
		try {
			url = new URL(urlToRead);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);

			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				html.append(line + "\n");
			}
			rd.close();
		} catch (Exception e) { // This could be java.net.SocketTimeoutException, but we'd do the same for any exception.
			// If connection fails, recurse with a different key_server
			Logger.error(e);
			return getPubKey(email, attempt + 1);
		}
		String xml = html.toString();

		// Standard xml builder
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;

		// this makes it fast
		try {
			dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}

		Document doc = null;

		try {
			InputSource is = new InputSource(new StringReader(xml));
			doc = dBuilder.parse(is);
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
		// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();

		NodeList nlist = doc.getElementsByTagName("pre");
		NodeList ObList;
		try {
			ObList = nlist.item(0).getChildNodes();
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}

		return ObList.item(0).getNodeValue();
	}
	
	/**
	 * This is a more modern function to replace getPubKey(String, int)
	 * This function is meant to call a Mailvelope style PGP key server. This calls it's more
	 * modern style API, rather than using the old HKP protocol (although it supports both).
	 * 
	 *  This function is far simpler, and this keyserver is far more secure as it requires authentication
	 *  of an email.
	 * 
	 * @param email
	 * @return PGP Public Key as a String
	 * @throws UnsupportedEncodingException
	 */
	private static String getPubKey(String email){
		
		StringBuilder json = new StringBuilder();
		URL url = null;
		BufferedReader rd;
		String line;
		// Attempt connection to server
		try {
			url = new URL(Config.Pgp.key_server + email);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);

			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				json.append(line + "\n");
			}
			rd.close();
		} catch (Exception e) { // This could be java.net.SocketTimeoutException, but we'd do the same for any exception.
			System.out.println(e);
		}
		
		JsonParser jp = new JsonParser();
		JsonObject jo = (JsonObject)jp.parse(json.toString());
		JsonElement je = jo.get("publicKeyArmored");
		return je.getAsString();
	}
}

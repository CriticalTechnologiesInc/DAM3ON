package core;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.pmw.tinylog.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import net.sf.classifier4J.summariser.SimpleSummariser;


/**
 * @author 
 * Justin Fleming - fleminjr@critical.com
 * Jeremy Fields - fieldsjd@critical.com
 * 
 *  This is a utilities class that contains helper methods to be
 *  used throughout various classes
 */

public class Utils {

	public static String getTextFromUrl(URL site){
        URLConnection connection;
		try {
			connection = site.openConnection();
	        BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                        connection.getInputStream()));
			
			StringBuilder response = new StringBuilder();
			String inputLine;
			
			while ((inputLine = in.readLine()) != null) 
			response.append(inputLine);
			
			in.close();
			return response.toString();
		} catch (IOException e) {
			return null;
		}

	}
	
	public static String extractEmail(Principal p){
		// Extract the common name (CN)
		int start = p.getName().indexOf("EMAILADDRESS");
		String tmpName, name = "";
		if (start >= 0) { 
		  tmpName = p.getName().substring(start+13);
		  int end = tmpName.indexOf(",");
		  if (end > 0) {
		    name = tmpName.substring(0, end);
		  }
		  else {
		    name = tmpName; 
		  }
		}
		
		return name;
	}
	
	
	public static X509Certificate loadNonExpiredCertificate(String certificate) throws FileNotFoundException, CertificateException {
		InputStream stream = new ByteArrayInputStream(certificate.getBytes(StandardCharsets.UTF_8));
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate x = (X509Certificate)cf.generateCertificate(stream);
		
		Date cur = new Date();
		if (cur.after(x.getNotAfter())){
			Logger.error("Cert is being used after it expired");
			return null;
		}
		
		if (cur.before(x.getNotBefore())){
			Logger.error("Cert is being used before it is valid");
			return null;
		}
		
		return x;
	}
	
	/**
	 * This method takes encodes a string to Base64
	 * 
	 * @param input
	 *            - string to be encoded
	 * @return Base64 encoded string
	 * @throws UnsupportedEncodingException
	 */
	public static String encodeBase64(String input){
		try {
			// for the love of God, utf-8 is supported. this try/catch really isn't necessary
			String tmp = Base64.getMimeEncoder().encodeToString(input.getBytes("utf-8"));
			tmp = tmp.replace("\n", "");
			tmp = tmp.replace("\r", "");
			return tmp;
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * Description:
	 * Turns a String[] into a String, based on a delimiter
	 * 
	 * @param arr String[]
	 * @param delimiter String
	 * @return String
	 */
	public static String stringArrToString(String[] arr, String delimiter){
		StringBuilder strBuilder = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
		   strBuilder.append(arr[i]);
		   if(i!=arr.length-1){
			   strBuilder.append(delimiter);
		   }
		}
		return strBuilder.toString();
	}
	
	/**
	 * This method takes encodes a byte[] to Base64
	 * 
	 * @param input
	 *            - byte[] to be encoded
	 * @return Base64 encoded string
	 * @throws UnsupportedEncodingException
	 */
	public static String encodeBase64(byte[] input) throws UnsupportedEncodingException {
		return Base64.getMimeEncoder().encodeToString(input);
	}
	
	
	/**
	 * Description:
	 * Splits a string byte newlines, and returns the resulting String[]
	 * 
	 * @param input String
	 * @return String[]
	 */
	public static String[] splitStringByNewlines(String input){
		return input.split("\\r\\n|\\n|\\r");
	}
	
	
	/**
	 * This method decodes a Base64 string
	 * 
	 * @param input
	 *            - base64 encoded string
	 * @return decoded string
	 */
	public static String decodeBase64(String input) {
		return new String(Base64.getMimeDecoder().decode(input));
	}
	
	/**
	 * This method returns a boolean based on if the input String
	 * is base64 encoded or not. Only works for "MIME Base64"
	 * 
	 * @param input
	 * @return boolean
	 */
	public static boolean isStringB64Encoded(String input){
		Base64.Decoder decoder = Base64.getMimeDecoder();

		try {
		    decoder.decode(input.getBytes());
		    return true;
		} catch(IllegalArgumentException iae) {
		    return false;
		}
	}
	
	/**
	 * This method generates the random string that's required in signatures.
	 * 
	 * @return String that is a 25 character long random sequence of uppercase letters, and 0-9.
	 */
	public static String generateRandomString(int count){
		final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}
	
	
	/**
	 * Util method for posting to Capraro's web service. Can be used for
	 * requesting roles and/or predicates. Sends a POST request over SSL to the
	 * endpoint URL. Receives a JSON object in return.
	 * 
	 * @param endPoint
	 *            - url we are POSTing to
	 * @param predicate
	 *            - predicate we want to retrieve (null if requesting a role)
	 * @param user
	 *            - user id to be used in query string
	 * @param token
	 *            - appropriate token for in query string
	 * @return a string array of the response JSON object
	 */
	public static String[] sslRequest(String endPoint, String predicate, String user, String token) {
		try {
			System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
			java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
			URL url = new URL(null, endPoint, new sun.net.www.protocol.https.Handler());

			// This is for bypassing certificate validation (SSL) //TODO DON'T USE IN PRODUCTION
			javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {
				public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
					return true;
				}
			});

			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");

			String query = "user=" + user;
			query += "&token=" + token;
			if (!(predicate == null)) {
				query += "&predicate=" + predicate;
			}
			Logger.info("PIP query to Capraro DB: {}", query);

			connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 3.0; Windows 3.1; CTI)");

			// open up the output stream of the connection
			DataOutputStream output = new DataOutputStream(connection.getOutputStream());

			// write out the data
			output.writeBytes(query);
			output.close();
			if (connection.getResponseCode() != 200) {
				Logger.error("Response code was: {}", connection.getResponseCode());
				return null;
			}
			// get ready to read the response from the cgi script
			DataInputStream input = new DataInputStream(connection.getInputStream());
			StringBuffer inputLine = new StringBuffer();

			// read in each character until end-of-stream is detected
			for (int c = input.read(); c != -1; c = input.read())
				inputLine.append((char) c);
			input.close();

			String result = inputLine.toString();
			JsonArray jArray = new JsonParser().parse(result).getAsJsonObject().getAsJsonArray("response");
			Logger.debug("JSON object: {}", inputLine);

			String things[] = new String[jArray.size()];
			for (int i = 0; i < jArray.size(); i++) {
				if (!jArray.get(i).isJsonNull()) {
					things[i] = jArray.get(i).getAsString();
				}
			}
			return things;
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}

	}

	/**
	 * This function instantiates, and if successful, returns a Properties
	 * object which can be used to get values from a configuration file.
	 * 
	 * @return Properties || null
	 */
	public static Properties readConfigGen(String path) {
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(path);
			// load a properties file
			prop.load(input);
			return prop;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	

	
	
	/**
	 * This method reads a file at a given path and returns the contents of the file as a String
	 * @param path full path of file to read
	 * @return String of file contents
	 * @throws IOException
	 */
	public static String readFile(String path)	{
		  byte[] encoded = null;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
			return new String(encoded, StandardCharsets.UTF_8);
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * This method reads a file at a given path and returns the contents of the file as a String
	 * @param File file to read
	 * @return String of file contents
	 * @throws IOException
	 */
	public static String readFile(File fd)	{
		  byte[] encoded = null;
		try {
			encoded = Files.readAllBytes(fd.toPath());
			return new String(encoded, StandardCharsets.UTF_8);
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * This method takes a String and writes it to a specified file
	 * @param path - full pathname to the file 
	 * @param content - String to be written to the file
	 * @throws IOException
	 */
	public static boolean writeFile(String path, String content) throws IOException{
		File file = new File(path);
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file);
			fileWriter.write(content);
			fileWriter.flush();
			fileWriter.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	
	/**
	 * Description:
	 * Deletes a file from the file system
	 * 
	 * @param path String of a valid path to a file
	 * @return boolean success
	 */
	public static boolean deleteFile(String path){
		return new File(path).delete();
	}
	

	/**
	 * Description:
	 * Returns the SHA1 binary digest of a given byte[]
	 * 
	 * @param b byte[] to be hashed
	 * @return byte[] binary digest
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] byteSha(byte[] b) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
			return md.digest(b);
		} catch (NoSuchAlgorithmException e) {
			Logger.error(e.toString());
			return null;
		}
	}

	
	/**
	 * Description:
	 * Formats a byte[] into a hexadecimal string
	 * 
	 * @param a byte[]
	 * @return string of the byte[] formatted as hexadecimal
	 */
	public static String byteArrayToHex(byte[] a) {
		   StringBuilder sb = new StringBuilder(a.length * 2);
		   for(byte b: a)
		      sb.append(String.format("%02x", b));
		   return sb.toString();
	}
	
	/**
	 * Description:
	 * This function converts a hexadecimal string to binary
	 * 
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] hexToBin(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}

	/**
	 * Description:
	 * Concatenates n byte[]'s
	 * 
	 * @param args - variable amount of byte[] to be concatenated (in order of args parameter)
	 * @return byte[] of concatenated a+b+c+...
	 * @throws IOException
	 */
	public static byte[] concatBytes(byte[]...args){
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		for(byte[] thing: args){
			try {
				outputStream.write(thing);
			} catch (IOException e) {
				Logger.error("Error concatenating bytes: {}", e.toString());
				return null;
			}
		}
		return outputStream.toByteArray();
	}
	
	
	/**
	 * Description:
	 * Checks whether a given path is valid & to a file.
	 * 
	 * @param path String of a path to a file
	 * @return boolean Valid or not
	 */
	public static boolean validFilePath(String path){
		if(path == null || path.isEmpty())
			return false;
		File f = new File(path);
		return f.exists() && f.isFile();
	}
	
	
	/**
	 * Description:
	 * Checks whether a given path is valid & to a directory
	 * 
	 * @param path String of a path to a directory
	 * @return boolean Valid or not
	 */
	public static boolean validDirectoryPath(String path){
		if(path == null || path.isEmpty())
			return false;
		File f = new File(path);
		return f.exists() && f.isDirectory();
	}
	
	/**
	 * Description:
	 * This checks whether a given string is a valid SHA1 hex digest
	 * http://stackoverflow.com/questions/1896715/how-do-i-check-if-a-string-is-a-valid-md5-or-sha1-checksum-string
	 * 
	 * @param s String of hexadecimal SHA1 digest
	 * @return
	 */
	public static boolean isValidSHA1(String s) {
		if(s != null)
			return s.matches("[a-fA-F0-9]{40}");
		return false;
	}
	
	/**
	 * Description:
	 * Returns the binary SHA1 digest of a file on the file system
	 * 
	 * @param fileName String of a path to a file
	 * @return byte[] binary SHA1 digest
	 * @throws Exception
	 */
	public static byte[] fileSha(String filePath) throws Exception {
		if(!validFilePath(filePath))
			return null;
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		FileInputStream ios = new FileInputStream(filePath);
		byte[] buffer = new byte[8 * 1024];
		int read = 0;
		while ((read = ios.read(buffer)) > 0)
			md.update(buffer, 0, read);
		ios.close();
		return md.digest();
	}
	
	/**
	 * Description:
	 * Checks whether the OS is Windows
	 * 
	 * @return boolean
	 */
	public static boolean isWindows() {
		return (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0);
	}

	/**
	 * Description:
	 * Checks whether the OS is Macintosh
	 * 
	 * @return boolean
	 */
	public static boolean isMac() {
		return (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0);
	}

	/**
	 * Description:
	 * Checks whether the OS is *nix
	 * 
	 * @return boolean
	 */
	public static boolean isUnix() {
		String tmp = System.getProperty("os.name").toLowerCase();
		return (tmp.indexOf("nix") >= 0 || tmp.toLowerCase().indexOf("nux") >= 0 || tmp.toLowerCase().indexOf("aix") > 0 );
	}

	/**
	 * Description:
	 * Checks whether the OS is Solaris
	 * 
	 * @return boolean
	 */
	public static boolean isSolaris() {
		return (System.getProperty("os.name").toLowerCase().indexOf("sunos") >= 0);
	}

	/**
	 * Description:
	 * Print's periods to the screen, separated by some delay.
	 * 
	 * @param howmany int of how many periods to print
	 * @param delay_ms int of how many milliseconds to wait between each one
	 */
	public static void loadingDots(int howmany, int delay_ms){

		for(int i = 0; i<howmany; i++){
			System.out.print(".");
			try {
				TimeUnit.MILLISECONDS.sleep(new Long(delay_ms));
			} catch (InterruptedException e) {}
		}
		
	}
	
	/**
	 * Description:
	 * Converts an InputStream into a String
	 * 
	 * @param input InputStream
	 * @return String
	 * @throws IOException
	 */
	public static String readInputStream(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }
	
	/**
	 * Description:
	 * Converts a file into a byte array
	 * 
	 * @param filename as a String
	 * @return a byte[] of the file
	 */
	public static byte[] readFileIntoByteArray(String filename){
		if(validFilePath(filename)){
			try {
				InputStream is = new FileInputStream(new File(filename));
				return IOUtils.toByteArray(is);
			} catch (IOException e) {
				return null;
			}
		}else{
			return null;
		}
	}
	
	/**
	 * Description:
	 * Takes in a byte[], gzip compresses it, and returns the resulting byte[]
	 * 
	 * @param input as byte[]
	 * @return output as byte[]
	 * @throws IOException
	 */
	public static byte[] gzipCompress(byte[] input) throws IOException{
		byte[] buffer = new byte[1024];
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gzos = new GZIPOutputStream(baos);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(input);
		
		int len;
		while ((len = bais.read(buffer)) > 0) {
	       	gzos.write(buffer, 0, len);
	    }
		
		bais.close();
		gzos.finish();
	    gzos.close();
	    
	    return baos.toByteArray();
	}
	
	
	/**
	 * Description:
	 * Takes in a byte[] and a file name, and compresses it using ZIP, and returns the result as a byte[]
	 * 
	 * @param input file as byte[]
	 * @param filename String of filename
	 * @return byte[] of ZIP compressed file
	 * @throws IOException
	 */
	public static byte[] zipCompressSingleFile(byte[] input, String filename) throws IOException{
		
		byte[] buffer = new byte[1024];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(baos);
		
		ZipEntry ze = new ZipEntry(filename);
		zos.putNextEntry(ze);
		ByteArrayInputStream bais = new ByteArrayInputStream(input);
		int len;
		while ((len = bais.read(buffer)) > 0) {
			zos.write(buffer, 0, len);
		}
	
		bais.close();
		zos.closeEntry();
	
		//remember close it
		zos.close();
		
		return baos.toByteArray();
	}
	
	
	/**
	 * Description:
	 * 
	 * Takes a String as input, summarizes it to the specified number of sentences
	 * then returns the resulting String
	 * 
	 * @param content as String
	 * @param numSentences as int
	 * @return String of summarized content
	 */
	public static String textSummariser(String content, int numSentences){
		SimpleSummariser ss = new SimpleSummariser();
		return ss.summarise(content, numSentences);
	}
}

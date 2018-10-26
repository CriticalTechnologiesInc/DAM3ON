package testsuite;

import java.io.File;
import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import core.Utils;

import testsuite.Config;

/**
 * @author Jeremy Fields - fieldsjd@critical.com
 *
 */
public class TestConfig {

	public static void main(String[] args)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		String confPath = Utilities.getConfigPathFromUser();

		System.out.println("--------------------------------------");

		// Instatiate Config object
		final Config c = new Config(confPath);
		// Get class object, and sub classes from Config
		Class<?>[] classes = c.getClass().getClasses();

		TestBasic(Config.Basic.log, Config.Basic.xsd, Config.Basic.exig, String.valueOf(Config.Basic.debug));
		TestKeystore(Config.Keystore.keystore_path, Config.Keystore.store_pass, Config.Keystore.keystore_alias, Config.Keystore.key_pass);
		TestSaml(Config.Saml.Assertion.issuer, Config.Saml.Assertion.subject_name_id, Config.Saml.Assertion.subject_schema_location, Config.Saml.request_id, Config.Saml.request_destination, Config.Saml.request_issuers, Config.Saml.response_issuers);
		TestPgp(Config.Pgp.pgp_tmp_dir, Config.Pgp.pdp_pubkey, Config.Pgp.key_servers);
	
		// This is so that testing Databases can be dynamic. We pass all sub classes of Databases
		// to the testing function, which can loop over each one to test. Wish there was easier way to
		// get Databases sub classes rather than having to loop to find it.
		for (Class<?> class_tmp : classes) {
			 if (class_tmp.getSimpleName().equals("Databases")) {

				Class<?>[] db_classes = class_tmp.getClasses();
				TestDatabases(db_classes);

			} 
		}
	}

	private static void TestPgp(String tmp_dir, String pdp_key, List<String> key_servers) {
		System.out.println();
		if ((tmp_dir == null) && (pdp_key == null) && (key_servers == null)) {
			System.out.println("PGP: N/A");
		} else {
			
			if ((tmp_dir == null) || tmp_dir.isEmpty() || (pdp_key == null) || pdp_key.isEmpty() || (key_servers == null) || key_servers.isEmpty()) {
				System.out.println("PGP: VERIFY. Not all values are required. Manually check the following values.");
				System.out.println("\t" + "pgp.pgp_tmp_dir: " + tmp_dir);
				System.out.println("\t" + "pgp.pdp_pubkey: " + pdp_key);
				System.out.println("\t" + "pgp.key_servers: " + key_servers);
			} else {
				boolean ks_size = (key_servers.size() > 0);
				boolean tmp_dir_check = Utils.validDirectoryPath(tmp_dir);
				boolean pdp_key_check = Utils.validFilePath(pdp_key);
				
				if (ks_size && tmp_dir_check && pdp_key_check) {
					System.out.println("PGP: PASSED");
				} else {
					System.out.println("PGP: FAILED. Check the following values.");
					System.out.println("\t" + "Valid pgp_tmp_dir: " + tmp_dir_check);
					System.out.println("\t" + "Valid pdp_pubkey: " + pdp_key_check);
					System.out.println("\t" + "Key servers: " + key_servers);
				}
			}
		}

	}

	private static void TestBasic(String log_path, String xsd_path, String exig_path, String dbg) {
		System.out.println();
		boolean log = Utils.validFilePath(log_path);
		boolean exig = Utils.validFilePath(exig_path);
		boolean xsd = Utils.validFilePath(xsd_path);

		if (log && xsd && exig) {
			System.out.println("BASIC: PASSED.");
		} else {
			System.out.println("BASIC: FAILED. Check the following paths");
			System.out.println("\tlog: " + log_path);
			System.out.println("\texig: " + exig_path);
			System.out.println("\txsd: " + xsd_path);
		}
		System.out.println("\tVERIFY: debug is set to: " + dbg);

	}

	private static void TestKeystore(String ks_path, String store_pass, String ks_alias, String k_pass) {
		System.out.println();
		if ((ks_path == null) || ks_path.isEmpty() || (store_pass == null) || store_pass.isEmpty() || (ks_alias == null)
				|| ks_alias.isEmpty() || (k_pass == null) || k_pass.isEmpty()) {
			System.out.println("KEYSTORE: FAILED - One of the following values is invalid:");
			System.out.println("\tkeystore_path: " + ks_path);
			System.out.println("\tstore_pass: " + store_pass);
			System.out.println("\tkeystore_alias: " + ks_alias);
			System.out.println("\tkey_pass: " + k_pass);
			return;
		}

		boolean ks_path_b = Utils.validFilePath(ks_path);
		boolean key_exists = false;
		// Check keystore exists
		if (ks_path_b) {
			try {
				File file = new File(ks_path);
				FileInputStream is = new FileInputStream(file);
				KeyStore ks_o = KeyStore.getInstance(KeyStore.getDefaultType());
				String password = store_pass;
				ks_o.load(is, password.toCharArray());

				// Check key exists
				@SuppressWarnings("rawtypes")
				Enumeration enumeration = ks_o.aliases();
				while (enumeration.hasMoreElements()) {
					String alias = (String) enumeration.nextElement();
					if (alias.equals(ks_alias.toLowerCase())) {
						key_exists = true;
					}
				}

				if (key_exists) {
					// Check key password
					@SuppressWarnings("unused")
					Key key = ks_o.getKey(ks_alias.toLowerCase(), k_pass.toCharArray());

					System.out.println("KEYSTORE: PASSED");
				} else {
					System.out.println("KEYSTORE: FAILED");
					System.out.println("\t'" + ks_alias.toLowerCase() + "' does not exist in keystore");
				}

			} catch (Exception e) {
				System.out.println("KEYSTORE: FAILED.");
			}
		} else {
			System.out.println("KEYSTORE: FAILED");
			System.out.println("\tKeystore path was invalid. Path is currently set to: " + ks_path);
		}
	}

	private static void TestSaml(String assIssuer, String assSNID, String assSchemaLoc, String rid, String rd,
			List<String> requestIssuers, List<String> responseIssuers) {
		System.out.println();
		boolean check1 = true;
		boolean check2 = true;
		boolean check3 = true;

		if ((assIssuer == null) || (assSNID == null) || (assSchemaLoc == null) || (rid == null) || (assIssuer.isEmpty())
				|| (assSNID.isEmpty()) || (assSchemaLoc.isEmpty()) || (rid.isEmpty())) {
			check1 = false;
		}

		if (!((rd == null) || rd.isEmpty())) {
			if (!rd.equals(responseIssuers.get(0))) {
				check2 = false;
			}
		}

		if ((requestIssuers == null) || (responseIssuers == null) || (requestIssuers.isEmpty())
				|| (responseIssuers.isEmpty())) {
			check3 = false;
		}

		if (!check1) {
			System.out.println("SAML: FAILED");
			System.out.println("\tCheck the following values: ");
			System.out.println("\tassertion.issuer: " + assIssuer);
			System.out.println("\tassertion.subject_name_id: " + assSNID);
			System.out.println("\tassertion.subject_schema_location: " + assSchemaLoc);
			System.out.println("\tassertion.request_id: " + rid);
		} else if (!check2) {
			System.out.println("SAML: VERIFY");
			System.out.println("\tThis appears to be a PEP, but request_destination does not match response_issuer");
		} else if (!check3) {
			System.out.println("SAML: FAILED");
			System.out.println("\tEither request_issuers or response_issuers is empty");
		} else {
			System.out.println("SAML: PASSED");
		}

	}

	private static void TestDatabases(Class<?>[] dbs)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		System.out.println();
		System.out.println("Testing Databases: ");

		for (Class<?> db : dbs) {

			String u = (String) db.getField("user").get(null);
			String p = (String) db.getField("password").get(null);
			String d = (String) db.getField("db_url").get(null);
			String j = (String) db.getField("jdbc_driver").get(null);

			// crazyness
			try {

				if ((u == null) || u.isEmpty() || (p == null) || p.isEmpty() || (d == null) || d.isEmpty()
						|| (j == null) || j.isEmpty()) {
					if ((u == null) && (p == null) && (d == null) && (j == null))
						System.out.println("\t" + db.getSimpleName() + ": N/A");
					else {
						System.out.println("\t" + db.getSimpleName() + ": FAILED - Check the following values");
						System.out.println("\t\tuser: " + u);
						System.out.println("\t\tpassword: " + p);
						System.out.println("\t\tdb_url: " + d);
						System.out.println("\t\tjdbc_driver: " + j);
					}
				} else {
					boolean connected = connectionTest(u, p, d, j);
					if (connected)
						System.out.println("\t" + db.getSimpleName() + ": PASSED");
					else
						System.out.println("\t" + db.getSimpleName() + ": FAILED");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("\t" + db.getSimpleName() + ": FAILED");
			}
		}

	}

	private static boolean connectionTest(String username, String password, String db_url, String driver) {
		Properties connectProperties = new Properties();
		connectProperties.put("user", username);
		connectProperties.put("password", password);
		connectProperties.put("useSSL", "false");
		Connection conn = null;

		try {
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(db_url, connectProperties);
			conn.close();
			return true;
		} catch (Exception e) {
			// e.printStackTrace();
			return false;
		}

	}

}

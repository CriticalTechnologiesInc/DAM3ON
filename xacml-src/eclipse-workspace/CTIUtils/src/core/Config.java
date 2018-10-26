package core;

import java.util.List;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;


public class Config {
	private static XMLConfiguration config;

	static{
		new Config();
	}

	private Config() {
		Parameters params = new Parameters();
		
//		String path = "c:\\users\\advan\\desktop\\config.xml";
//		String path = "C:\\Users\\Jeremy\\Google Drive\\CTI\\darpa\\temp_xacml3_webxacml\\config.xml";
		String path = "/etc/webxacml/config.xml";
		
		FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<XMLConfiguration>(
				XMLConfiguration.class).configure(params.xml().setFileName(path));
		try {
			config = builder.getConfiguration();
		} catch (ConfigurationException e) {
			System.out.println("Error building config object");
			e.printStackTrace();
			config = null;
		}
	}
	
	public Config(String path) {
		Parameters params = new Parameters();
		
		FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<XMLConfiguration>(
				XMLConfiguration.class).configure(params.xml().setFileName(path));
		try {
			config = builder.getConfiguration();
		} catch (ConfigurationException e) {
			System.out.println(e);
			System.out.println("Error building config object");
			config = null;
		}
	}
	
	public static String getCustomFieldAsString(String key){
		return config.getString(key);
	}
	
	public static boolean getCustomFieldAsBoolean(String key){
		return Boolean.parseBoolean(config.getString(key));
	}
	
	public static class Basic{
		private static final String ns = "basic.";
		
		public static final String log = config.getString(ns+"log");
		public static final boolean debug = Boolean.parseBoolean(config.getString(ns+"debug"));
		public static final String xsd = config.getString(ns+"xsd");
		public static final String exig = config.getString(ns+"exig");
	}
	
	public static class Databases {
		private static final String ns = "databases.";
		
		public static class SSL {
			private static final String ssl_ns = ns + "ssl.";
			
			public static final String keystore_path = config.getString(ssl_ns + "keystore_path");
			public static final String keystore_pw = config.getString(ssl_ns + "keystore_pw");
		}
		
		public static class PolicyDB {
			private static final String pdb_ns = ns + "policy.";

			public static final String user = config.getString(pdb_ns + "user");
			public static final String password = config.getString(pdb_ns + "password");
			public static final String jdbc_driver = config.getString(pdb_ns + "jdbc_driver");
			public static final String db_url = config.getString(pdb_ns + "db_url");
			public static final String policy_table = config.getString(pdb_ns + "policy_table");
			public static final String selected_policies_table = config.getString(pdb_ns + "selected_policies_table");
		}
		public static class RoleDB {
			private static final String rdb_ns = ns + "role.";

			public static final String user = config.getString(rdb_ns + "user");
			public static final String password = config.getString(rdb_ns + "password");
			public static final String jdbc_driver = config.getString(rdb_ns + "jdbc_driver");
			public static final String db_url = config.getString(rdb_ns + "db_url");
			public static final String table = config.getString(rdb_ns + "table");
		}
		
		public static class privateNonceDB{
			private static final String pnonce_ns = ns + "pnonce.";
			
			public static final String user = config.getString(pnonce_ns + "user");
			public static final String password = config.getString(pnonce_ns + "password");
			public static final String jdbc_driver = config.getString(pnonce_ns + "jdbc_driver");
			public static final String db_url = config.getString(pnonce_ns + "db_url");
		}
		
		public static class publicNonceDB{
			private static final String pub_nonce_ns = ns + "pub_nonce.";
			
			public static final String user = config.getString(pub_nonce_ns + "user");
			public static final String password = config.getString(pub_nonce_ns + "password");
			public static final String jdbc_driver = config.getString(pub_nonce_ns + "jdbc_driver");
			public static final String db_url = config.getString(pub_nonce_ns + "db_url");
		}
		
		public static class tokenDB{
			private static final String token_ns = ns + "token.";
			
			public static final String user = config.getString(token_ns + "user");
			public static final String password = config.getString(token_ns + "password");
			public static final String jdbc_driver = config.getString(token_ns + "jdbc_driver");
			public static final String db_url = config.getString(token_ns + "db_url");
		}
		
		public static class alertDB{
			private static final String token_ns = ns + "alert.";
			
			public static final String user = config.getString(token_ns + "user");
			public static final String password = config.getString(token_ns + "password");
			public static final String jdbc_driver = config.getString(token_ns + "jdbc_driver");
			public static final String db_url = config.getString(token_ns + "db_url");
		}
		
		public static class approved_file_db{
			private static final String token_ns = ns + "file.";
			
			public static final String user = config.getString(token_ns + "user");
			public static final String password = config.getString(token_ns + "password");
			public static final String jdbc_driver = config.getString(token_ns + "jdbc_driver");
			public static final String db_url = config.getString(token_ns + "db_url");
		}
	
	}
	
	public static class Keystore {
		private static final String ns = "keystore.";

		public static final String key_pass = config.getString(ns + "key_pass");
		public static final String store_pass = config.getString(ns + "store_pass");
		public static final String keystore_alias = config.getString(ns + "keystore_alias");
		public static final String keystore_path = config.getString(ns + "keystore_path");
		
		public static final List<String> ultimate_trusted_issuers = config.getList(String.class, ns + "ultimate_trusted_issuers.issuer");
		
	}

	public static class Saml {
		private static final String ns = "saml.";
		
		public static final List<String> request_issuers = config.getList(String.class, ns + "request_issuers.issuer");
		public static final List<String> response_issuers = config.getList(String.class, ns + "response_issuers.issuer");

		public static final String request_destination = config.getString(ns + "request_destination");
		public static final String request_id = config.getString(ns + "request_id");
		
		public static class Assertion {
			private static final String ans = ns + "assertion.";

			public static final String issuer = config.getString(ans + "issuer");
			public static final String subject_name_id = config.getString(ans + "subject_name_id");
			public static final String subject_schema_location = config.getString(ans + "subject_schema_location");
		}
	}
	
	public static class Certificates{
		private static final String ns = "certs.";
		
		public static final int depth = config.getInt(ns + "depth");
		public static final int max_properties = config.getInt(ns + "max_properties");
		public static final int required_insurance_amount = config.getInt(ns + "required_insurance_amount");
		public static final boolean accepting_untrusted = config.getBoolean(ns + "accepting_untrusted");
		public static final boolean add_evidence = config.getBoolean(ns + "add_evidence");
		public static final String ca_path = config.getString(ns + "ca_path");
	}
	
	public static class Tpm {
		private static final String ns = "tpm.";
		
		public static final String tmp_dir = config.getString(ns + "tmp_dir");
	}
	
	
	public static class Pgp{
		private static String ns = "pgp.";
		
		public static final String pdp_pubkey = config.getString(ns+"pdp_pubkey");
		public static final String pgp_tmp_dir = config.getString(ns+"pgp_tmp_dir");
		public static final String token_private_key = config.getString(ns+"token_private_key");
		public static final String token_private_key_pw = config.getString(ns+"token_private_key_pw");
		public static final String key_server = config.getString(ns+"key_server");
		public static final List<String> key_servers = config.getList(String.class, ns + "key_servers.server");
	}
	
	
	/* Higher level classes below, contain 'modules' above */
	public static class Pdp {
		/* Load PDP specific properties */
		// This is unused
		public static final List<String> assert_role_not_required = config.getList(String.class, "assert_role_not_required.issuer");
		public static final String issuer_mappings = config.getString("issuer_mappings");
		public static final String policy_template = config.getString("policy_template");
	}

	public static class DecryptPep {
		/* Load this PEP's specific properties */
		public static final String web_pdp_url = config.getString("web_pdp_url");
		public static final String annex_name = config.getString("annex_name");
		public static final String tahoe_name = config.getString("tahoe_name");
	}

	public static class MultiLoginPep {
		/* Load this PEP's specific properties */
		public static final String web_pdp_url = config.getString("web_pdp_url");
		public static final String hashcheck_url = config.getString("hashcheck_url");
		public static final String pub_nonce_api = config.getString("pub_nonce_api");
		public static final String tahoe_name = config.getString("tahoe_name");
	}
	
	public static class Mailman{
		/* Load the Mailman's PEP's specific properties */
		private static final String ns = "mailman.";
		
		public static final String enforcer_location = config.getString(ns+"enforcer_location");
	}
	
	public static class Tailor{
		/* Load the Tailoring Services specific properties */
		private static final String ns = "tailor.";
		
		public static final String token = config.getString(ns+"token");
		public static final String postfix = config.getString(ns+"postfix"); // The IP that postfix binded to (may not be localhost!!)
		public static final String top_mm_domain = config.getString(ns+"top_mm_domain");
		public static final String tahoe_endpoint = config.getString(ns+"tahoe_endpoint");
		public static final String web_domain = config.getString(ns+"web_domain");
		public static final String cap_xsl_path = config.getString(ns+"cap_xsl_path");
	}
	
}

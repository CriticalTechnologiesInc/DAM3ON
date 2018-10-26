package testsuite;

import java.util.List;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;


public class Config {
	private static XMLConfiguration config;
	
	public Config(String path) {
		Parameters params = new Parameters();
		
		FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<XMLConfiguration>(
				XMLConfiguration.class).configure(params.xml().setFileName(path));
		try {
			config = builder.getConfiguration();
		} catch (ConfigurationException e) {
			System.out.println("Error building config object");
			config = null;
		}
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
		
	}
	
	public static class Keystore {
		private static final String ns = "keystore.";

		public static final String key_pass = config.getString(ns + "key_pass");
		public static final String store_pass = config.getString(ns + "store_pass");
		public static final String keystore_alias = config.getString(ns + "keystore_alias");
		public static final String keystore_path = config.getString(ns + "keystore_path");
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
	
	
	public static class Tpm {
		private static final String ns = "tpm.";
		
		public static final String tmp_dir = config.getString(ns + "tmp_dir");
		public static final String hash_dir = config.getString(ns + "hash_dir");
	}
	
	
	public static class Pgp{
		private static String ns = "pgp.";
		
		public static final String pdp_pubkey = config.getString(ns+"pdp_pubkey");
		public static final String pgp_tmp_dir = config.getString(ns+"pgp_tmp_dir");
		public static final List<String> key_servers = config.getList(String.class, ns + "key_servers.server");
	}
	
	
	/* Higher level classes below, contain 'modules' above */
	public static class Pdp {
		/* Load PDP specific properties */
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
		public static final String pub_nonce_api = config.getString("pub_nonce_api");
		public static final String tahoe_name = config.getString("tahoe_name");
	}
	
}

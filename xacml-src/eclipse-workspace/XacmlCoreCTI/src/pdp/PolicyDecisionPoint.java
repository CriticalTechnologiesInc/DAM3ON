package pdp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.wso2.balana.PDPConfig;
import org.wso2.balana.cond.FunctionFactory;
import org.wso2.balana.cond.FunctionFactoryProxy;
import org.wso2.balana.cond.StandardFunctionFactory;
import org.wso2.balana.finder.AttributeFinder;
import org.wso2.balana.finder.AttributeFinderModule;
import org.wso2.balana.finder.PolicyFinder;
import org.wso2.balana.finder.PolicyFinderModule;
import org.wso2.balana.finder.impl.CurrentEnvModule;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;
import org.wso2.balana.finder.impl.SelectorModule;

import helpers.AuthFunction;
import helpers.DatabasePolicyModule;
import helpers.IpInfoLookupModule;
import helpers.IpListFunction;
import helpers.LocationInPolygonFunction;
import helpers.OnBehalfOfRoleModule;
import helpers.SecPropLookupModule;
import helpers.SmartCardAuth;
import helpers.SubjectRoleModule;
import helpers.VerifySubjectRoleModule;

public class PolicyDecisionPoint {
	public static PDPConfig getPDPConfigWithFileBasedPolicyFinder(String policyDirectory) {

		//////////// BEGIN POLICY STUFF ////////////
		System.setProperty(FileBasedPolicyFinderModule.POLICY_DIR_PROPERTY, policyDirectory);
		PolicyFinder policyFinder = new PolicyFinder();
		Set<PolicyFinderModule> policyFinderModules = new HashSet<PolicyFinderModule>();
		FileBasedPolicyFinderModule fileBasedPolicyFinderModule = new FileBasedPolicyFinderModule();
		policyFinderModules.add(fileBasedPolicyFinderModule);
		policyFinder.setModules(policyFinderModules);
		//////////// END POLICY STUFF ////////////

		//////////// BEGIN MODULE STUFF ////////////
		CurrentEnvModule currentEnvModule = new CurrentEnvModule();
		SelectorModule selectorModule = new SelectorModule();
		SubjectRoleModule subjectRoleModule = new SubjectRoleModule();
		OnBehalfOfRoleModule onBehalfOfModule = new OnBehalfOfRoleModule();
		VerifySubjectRoleModule verifySubjectRole = new VerifySubjectRoleModule();
		SecPropLookupModule secPropLookupModule = new SecPropLookupModule();
		IpInfoLookupModule ipInfoLookupModule = new IpInfoLookupModule();
		// UserCertificateRoleModule ucem = new UserCertificateRoleModule();
		// UserAgentModule userAgentModule = new UserAgentModule();

		// Create attribute finder and add modules to it
		AttributeFinder attributeFinder = new AttributeFinder();
		List<AttributeFinderModule> arrayList = new ArrayList<AttributeFinderModule>();
		arrayList.add((AttributeFinderModule) subjectRoleModule);
		arrayList.add((AttributeFinderModule) verifySubjectRole);
		arrayList.add((AttributeFinderModule) currentEnvModule);
		arrayList.add((AttributeFinderModule) selectorModule);
		arrayList.add((AttributeFinderModule) onBehalfOfModule);
		arrayList.add((AttributeFinderModule) secPropLookupModule);
		arrayList.add((AttributeFinderModule) ipInfoLookupModule);
		// arrayList.add((Object) ucem);
		// arrayList.add((Object) userAgentModule);

		attributeFinder.setModules(arrayList);
		//////////// END MODULE STUFF ////////////

		////////// BEGIN FUNCTION STUFF ////////////
		FunctionFactoryProxy proxy = StandardFunctionFactory.getNewFactoryProxy();
		FunctionFactory factory = proxy.getConditionFactory();
		factory.addFunction(new IpListFunction("ip-on-whitelist"));
		factory.addFunction(new IpListFunction("ip-not-on-blacklist"));
		factory.addFunction(new LocationInPolygonFunction("lat-long-in-polygon"));
		factory.addFunction(new LocationInPolygonFunction("lat-long-not-in-polygon"));
		factory.addFunction(new AuthFunction());
		factory.addFunction(new SmartCardAuth());
		FunctionFactory.setDefaultFactory(proxy);
		////////// END FUNCTION STUFF ////////////

		// Create and return the PDPConfig object using attribute finder and policy
		// finder created
		return new PDPConfig(attributeFinder, policyFinder, null, false);
	}

	public static PDPConfig getPDPConfigWithDBBasedPolicyFinder() {

		//////////// BEGIN POLICY STUFF ////////////
		DatabasePolicyModule dbPolicyModule = new DatabasePolicyModule();
		ArrayList<String> test = xacml.Util.readSelectedPoliciesFromDB();
		// For each policy file name given, add policy
		for (int i = 0; i < test.size(); ++i)
			dbPolicyModule.addPolicy(test.get(i));

		// Create the actual policy finder
		PolicyFinder policyFinder = new PolicyFinder();

		// Create hash set of FilePolicyModule type and add filePolicyModule to hash set
		HashSet<DatabasePolicyModule> hashSet = new HashSet<DatabasePolicyModule>();
		hashSet.add(dbPolicyModule);

		// Add policy finder module to policy finder
		policyFinder.setModules(hashSet);
		//////////// END POLICY STUFF ////////////

		//////////// BEGIN MODULE STUFF ////////////
		CurrentEnvModule currentEnvModule = new CurrentEnvModule();
		SelectorModule selectorModule = new SelectorModule();
		SubjectRoleModule subjectRoleModule = new SubjectRoleModule();
		OnBehalfOfRoleModule onBehalfOfModule = new OnBehalfOfRoleModule();
		VerifySubjectRoleModule verifySubjectRole = new VerifySubjectRoleModule();
		SecPropLookupModule secPropLookupModule = new SecPropLookupModule();
		IpInfoLookupModule ipInfoLookupModule = new IpInfoLookupModule();
		// UserCertificateRoleModule ucem = new UserCertificateRoleModule();
		// UserAgentModule userAgentModule = new UserAgentModule();

		// Create attribute finder and add modules to it
		AttributeFinder attributeFinder = new AttributeFinder();
		List<AttributeFinderModule> arrayList = new ArrayList<AttributeFinderModule>();
		arrayList.add((AttributeFinderModule) subjectRoleModule);
		arrayList.add((AttributeFinderModule) verifySubjectRole);
		arrayList.add((AttributeFinderModule) currentEnvModule);
		arrayList.add((AttributeFinderModule) selectorModule);
		arrayList.add((AttributeFinderModule) onBehalfOfModule);
		arrayList.add((AttributeFinderModule) secPropLookupModule);
		arrayList.add((AttributeFinderModule) ipInfoLookupModule);
		// arrayList.add((Object) ucem);
		// arrayList.add((Object) userAgentModule);

		attributeFinder.setModules(arrayList);
		//////////// END MODULE STUFF ////////////

		////////// BEGIN FUNCTION STUFF ////////////
		FunctionFactoryProxy proxy = StandardFunctionFactory.getNewFactoryProxy();
		FunctionFactory factory = proxy.getConditionFactory();
		factory.addFunction(new IpListFunction("ip-on-whitelist"));
		factory.addFunction(new IpListFunction("ip-not-on-blacklist"));
		factory.addFunction(new LocationInPolygonFunction("lat-long-in-polygon"));
		factory.addFunction(new LocationInPolygonFunction("lat-long-not-in-polygon"));
		factory.addFunction(new AuthFunction());
		factory.addFunction(new SmartCardAuth());
		FunctionFactory.setDefaultFactory(proxy);
		////////// END FUNCTION STUFF ////////////

		// Create and return the PDPConfig object using attribute finder and policy
		// finder created
		return new PDPConfig(attributeFinder, policyFinder, null, false);
	}
}

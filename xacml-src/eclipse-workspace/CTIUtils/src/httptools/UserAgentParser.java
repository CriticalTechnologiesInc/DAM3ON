package httptools;

import eu.bitwalker.useragentutils.UserAgent;

public class UserAgentParser {

	private static String browserTypeName;
	private static String browserManufacturerName;
	private static String browserName;
	private static String browserRenderEngineName;
	private static String browserVersion;
	private static String osMan;
	private static String osName;
	private static String deviceType;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		UserAgentParser ua = new UserAgentParser("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
		System.out.println("Browser Man Name:" + ua.getBrowserManufacturerName());
		System.out.println("Browser Name: " + ua.getBrowserName());
		System.out.println("Browser Render Engine: " + ua.getBrowserRenderEngineName());
		System.out.println("Browser Type Name: " + ua.getBrowserTypeName());
		System.out.println("Browser Version: " + ua.getBrowserVersion());
		System.out.println("Device Type: " + ua.getDeviceType());
		System.out.println("OS Man: " + ua.getOsMan());
		System.out.println("OS Name: " + ua.getOsName());
	}
	
	public UserAgentParser(String userAgent){
		UserAgent ua = UserAgent.parseUserAgentString(userAgent);
		browserTypeName = ua.getBrowser().getBrowserType().getName();
		browserManufacturerName = ua.getBrowser().getManufacturer().getName();
		browserName = ua.getBrowser().getName();
		browserRenderEngineName = ua.getBrowser().getRenderingEngine().toString();
		browserVersion = ua.getBrowser().getVersion(userAgent).getVersion();
		osMan = ua.getOperatingSystem().getManufacturer().getName();
		osName = ua.getOperatingSystem().getName();
		deviceType = ua.getOperatingSystem().getDeviceType().getName();
		

		
	}

	public String getBrowserTypeName() {
		return browserTypeName;
	}

	public String getBrowserManufacturerName() {
		return browserManufacturerName;
	}

	public String getBrowserName() {
		return browserName;
	}

	public String getBrowserRenderEngineName() {
		return browserRenderEngineName;
	}

	public String getBrowserVersion() {
		return browserVersion;
	}

	public String getOsMan() {
		return osMan;
	}

	public String getOsName() {
		return osName;
	}

	public String getDeviceType() {
		return deviceType;
	}

}

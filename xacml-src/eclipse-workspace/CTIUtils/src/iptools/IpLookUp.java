package iptools;

import java.util.Map;

import core.JsonParser;
import httptools.util;

/**
 * This class uses the freegeoip.net API for looking up location information
 * from an IP address.
 * 
 * @author Jeremy
 *
 */
public class IpLookUp {

	private String ip;
	private String country_code;
	private String country_name;
	private String region_code;
	private String region_name;
	private String city;
	private String zip_code;
	private String time_zone;
	private Double latitude;
	private Double longitude;
	private Integer metro_code;

	private static final String endpoint = "http://freegeoip.net/json/";
	// freegeoip.net limits to 10k queries per hour. it uses a free/open source
	// DB we could
	// download our selves if we ever hit that limit.

	public static void main(String[] args) {
		
		// Test ipLookUp:
		//192.168.2.24
//		IpLookUp ilu = new IpLookUp("209.217.211.175");
		IpLookUp ilu = new IpLookUp("192.168.2.24");

//		ipLookUp ilu = new ipLookUp("139.162.111.98"); // Tokyo, Japan
//		ipLookUp ilu = new ipLookUp("115.239.230.227"); // Shaoxing, China
//		ipLookUp ilu = new ipLookUp("91.236.75.4"); // (no city), Poland
//		ipLookUp ilu = new ipLookUp("60.191.38.78"); // Hangzhou, China
		System.out.println("Location: " + ilu.getLatitude()+","+ilu.getLongitude());
		System.out.println("City: " + ilu.getCity());
		System.out.println("Country Code: " + ilu.getCountry_code());
		System.out.println("Country Name: " + ilu.getCountry_name());
		System.out.println("IP Address: " + ilu.getIp());
		System.out.println("Region Code: " + ilu.getRegion_code());
		System.out.println("Region Name: " + ilu.getRegion_name());
		System.out.println("Time Zone: " + ilu.getTime_zone());
		System.out.println("Zip Code: " + ilu.getZip_code());
		System.out.println("Metro Code: " + ilu.getMetro_code());
	}
	
	/**
	 * Constructor for ipLookUp. Given an IP address OR hostname as a string,
	 * this object will set all its fields with the response information from
	 * lookupIp(), which can then be accessed by the getters.
	 * 
	 * @param ip
	 */
	public IpLookUp(String ip) {
		String json_response = util.doGetRequest(endpoint, ip);

		JsonParser jp = new JsonParser();
		Map<String, ?> map = jp.parseJson(json_response);
		
		if(map == null) // some API error ocurred
			return;

		this.ip = (String) map.get("ip");
		this.country_code = (String) map.get("country_code");
		this.country_name = (String) map.get("country_name");
		this.region_code = (String) map.get("region_code");
		this.region_name = (String) map.get("region_name");
		this.city = (String) map.get("city");
		this.zip_code = (String) map.get("zip_code");
		this.time_zone = (String) map.get("time_zone");
		
		try{
			this.latitude = (Double) map.get("latitude");
		}catch(ClassCastException cce){ // can return a 0 if IP is internal 
			this.latitude = ((Integer)map.get("latitude")).doubleValue();
		}
		
		try{
			this.longitude = (Double) map.get("longitude");
		}catch(ClassCastException cce){ // can return a 0 if IP is internal 
			this.longitude = ((Integer)map.get("longitude")).doubleValue();
		}
		
		this.metro_code = (Integer) map.get("metro_code");
	}
	

	public String getIp() {return ip;}
	public String getCountry_code() {return country_code;}
	public String getCountry_name() {return country_name;}
	public String getRegion_code() {return region_code;}
	public String getRegion_name() {return region_name;}
	public String getCity() {return city;}
	public String getZip_code() {return zip_code;}
	public String getTime_zone() {return time_zone;}
	public Double getLatitude() {return latitude;}
	public Double getLongitude() {return longitude;}
	public Integer getMetro_code() {return metro_code;}
}

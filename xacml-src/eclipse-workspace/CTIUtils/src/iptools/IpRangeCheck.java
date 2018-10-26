 package iptools;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class IpRangeCheck {

	public static void main(String[] args) throws UnknownHostException {
		String myip = "45.47.113.239";
		
		ArrayList<String> whitelist = new ArrayList<String>();
		whitelist.add("45.47.113.239/24/24");
		whitelist.add("209.217.211.160/24");
		
		ArrayList<String> blacklist = new ArrayList<String>();
		blacklist.add("209.217.211.160/24");
		
		
		try {
			System.out.println("Is on whitelist: " + isInRange(myip, whitelist));
			System.out.println("Is on blacklist: " + isInRange("209.217.211.175", blacklist));
		} catch (Exception e) {
			System.out.println("Bad input!");
		}
	}

	
	public static boolean isInRange(String given_s, String against_s, int range) throws Exception{
		int shift = 32 - range;
		
		BigInteger against;
		BigInteger given;
		try {
			against = new BigInteger(InetAddress.getByName(against_s).getAddress()).shiftRight(shift);
			given = new BigInteger(InetAddress.getByName(given_s).getAddress()).shiftRight(shift);
		} catch (UnknownHostException e) {
			throw new Exception();
		}

		// 0 == equal, -1 == less than, 1 == greater than
		return 0==given.compareTo(against) ? true : false;
	}
	
	public static boolean isInRange(String given_s, ArrayList<String> against_list) throws Exception{
		for(String againstAndRange : against_list){
			String[] agArr = againstAndRange.split("/");
			String against_s = agArr[0];
			int range = Integer.parseInt(agArr[1]);
			int shift = 32 - range;
			
			BigInteger against;
			BigInteger given;
			try {
				against = new BigInteger(InetAddress.getByName(against_s).getAddress()).shiftRight(shift);
				given = new BigInteger(InetAddress.getByName(given_s).getAddress()).shiftRight(shift);
			} catch (UnknownHostException e) {
				throw new Exception();
			}
			if(0==given.compareTo(against))
				return true;
		}
		return false;
		
	}
	

}

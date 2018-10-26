package httptools;

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

/** This is it's own class due to dependence on specific, external API **/

public class UrlShorten {

	/**
	 * Description:
	 * This uses an API key from criticaltest5@gmail.com for Google's URL shortening service.
	 * 
	 * Quotas are: 1mil requests per day AND 100 requests per second/user
	 * 
	 * @param longUrl String of the URL you wish to shorten
	 * @return String of the shortened URL
	 */
	public static String shortenUrl(String longUrl){
		final String GOOGLE_URL_SHORT_API = "https://www.googleapis.com/urlshortener/v1/url";
		final String GOOGLE_API_KEY = "AIzaSyCc06roPtW-FF_MCy9WO2BEt4pB7GwMh-I";
	    if (longUrl == null) {
	        return longUrl;
	    }else if(!longUrl.startsWith("http://") && !longUrl.startsWith("https://")){
	        longUrl = "http://"+longUrl;
	    }
	    try {
	        String json = "{\"longUrl\": \""+longUrl+"\"}";   
	        String apiURL = GOOGLE_URL_SHORT_API+"?key="+GOOGLE_API_KEY;
	         
	        HttpPost postRequest = new HttpPost(apiURL);
	        postRequest.setHeader("Content-Type", "application/json");
	        postRequest.setEntity(new StringEntity(json, "UTF-8"));
	 
	        CloseableHttpClient httpClient = HttpClients.createDefault();
	        HttpResponse response = httpClient.execute(postRequest);
	        String responseText = EntityUtils.toString(response.getEntity());           
	         
	        Gson gson = new Gson();
	        @SuppressWarnings("unchecked")
	        HashMap<String, String> res = gson.fromJson(responseText, HashMap.class);
	 
	        return res.get("id");            
	         
	    } catch (IOException e) {
	        return null;
	    }
	 }
	
}

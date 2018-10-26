package validator;

import org.pmw.tinylog.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ModFile {
	
	private String binSha1 = null;
	private String arg = null;
	private String name = null;
	private String certificateUrl = null;
	private int order = -1;
	private boolean hasBinSha1 = false;
	private boolean hasCertUrl = false;
	private boolean hasOrder = false;
	private boolean hasArg = false;
	private boolean hasName = false;

	public ModFile(JsonElement jFileInfo) {
		JsonObject tmp1 = jFileInfo.getAsJsonObject();
		
		// Always required
		try{
			this.order = tmp1.get("order").getAsInt();
			this.hasOrder = true;
		} catch (Exception e){
			Logger.error(e);
		}
		
		try{
			this.binSha1 = tmp1.get("bin-sha1").getAsString();
			this.hasBinSha1 = true;
		} catch (Exception e){
			Logger.error(e);
		}
		
		
		try{
			this.certificateUrl = tmp1.get("cert-url").getAsString();
			this.hasCertUrl = true;
		} catch (java.lang.NullPointerException npe){
			// is ok if null
		} catch (Exception e){
			Logger.error(e);
		}
	
		try{
			this.arg = tmp1.get("arg").getAsString();
//	     String h1ah = core.Utils.byteArrayToHex(core.Utils.byteSha(h1a.getBytes()));
//			this.arg = core.Utils.byteArrayToHex(core.Utils.byteSha(this.arg.getBytes()));
			this.hasArg = true;
		} catch (java.lang.UnsupportedOperationException uo){
			// is ok if null
		} catch (Exception e){
			Logger.error(e);
		}
		
		try{
			this.name = tmp1.get("name").getAsString();
			this.hasName = true;
		} catch (Exception e){
			Logger.error(e);
		}
	
		
	}
	
	public boolean hasBinSha1() {
		return hasBinSha1;
	}

	public boolean hasCertUrl() {
		return hasCertUrl;
	}

	public boolean hasOrder() {
		return hasOrder;
	}
	
	public boolean hasArg() {
		return hasArg;
	}
	
	public boolean hasName() {
		return hasName;
	}
	
	public String getBinSha1() {
		return binSha1;
	}

	public String getName() {
		return name;
	}
	
	public String getArg() {
		return arg;
	}
	
	public String getCertificateUrl() {
		return certificateUrl;
	}

	public int getOrder() {
		return order;
	}

}
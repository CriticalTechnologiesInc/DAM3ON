package httptools;

import java.util.ArrayList;

public class UrlParser {

	
	public static void main(String[] args) {
		ArrayList<String> als = new ArrayList<String>();
		als.add("www.google.com/hello#hi");
		als.add("http://www.example.com/a/b/c/d/e/f/g/h/i.html");
		als.add("http://www.example.com:8800");
		als.add("http://www.test.com?pageid=123&testid=1524");
		als.add("http://www.test.com/do.html#A");
		als.add("http://stackoverflow.com/users/9999999/not a-real-user");
		als.add("lol this. should not work at all");
			
		for(int i = 0; i < als.size(); i++){
			CTIURL cu = new CTIURL(als.get(i));
			if(cu.validish)
				System.out.println(cu.protocol + " " + cu.host + " " + cu.tld + "\n");
		}
		

	}
	
	public static class CTIURL{
		String[] protoSplit = null;
		String[] fSlashSplit = null;
		String[] periodSplit = null;
		String url = null;
		String host = null;
		String tld = null;
		String protocol = null;
		boolean validish = false;
		
		CTIURL(String urlIn){
			if(urlIn.contains(" ") || urlIn.length() < 4 || !urlIn.contains("."))
				return;
			
			this.url = urlIn;
			protoSplit = urlIn.split("://");
			
			if(protoSplit.length < 2)
				this.protocol = null;
			else
				this.protocol = protoSplit[0];
			
			if(this.protocol != null)
				fSlashSplit = protoSplit[1].split("/");
			else
				fSlashSplit = urlIn.split("/");
				
			if(fSlashSplit.length > 0)
				periodSplit = fSlashSplit[0].split("\\.");
			else
				periodSplit = urlIn.split(".");	

			if(periodSplit.length > 1 ){
				this.host = periodSplit[periodSplit.length - 2];
				
				if(periodSplit[periodSplit.length - 1].contains(":") && periodSplit[periodSplit.length - 1].contains("?") || periodSplit[periodSplit.length - 1].contains(":"))
					this.tld = periodSplit[periodSplit.length - 1].split(":")[0];
				else if(periodSplit[periodSplit.length - 1].contains("?"))
					this.tld = periodSplit[periodSplit.length - 1].split("\\?")[0];
				else
					this.tld = periodSplit[periodSplit.length - 1];
				
				if(this.tld != null && this.host != null)
					validish = true;
			}
		}
		
	}
	
}


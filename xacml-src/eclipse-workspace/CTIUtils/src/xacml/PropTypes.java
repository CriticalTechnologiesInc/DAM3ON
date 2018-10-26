package xacml;
public enum PropTypes{
	FUCKYOU("fuckyou-trustme"),
	PROOF("proof"),
	INSURED("insured"),
	PENTEST("pentested"),
	REF("reference");
	
	 private String type;

	 PropTypes(String type) {this.type = type;}
     public String type() {return type;}
}
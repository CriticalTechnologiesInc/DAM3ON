package xacml;

public class Property{
	public Property(String name, String val, PropTypes type){
		this.propertyName = name;
		this.propertyValue = val;
		this.type = type;
	}
	
	public Property(){
		this.propertyName = null;
		this.propertyValue = null;
		this.type = null;
	}
	public String propertyName;
	public String propertyValue;
	public PropTypes type;
}
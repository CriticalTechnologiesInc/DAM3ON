package xacml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

import core.CTIConstants;

/**
 * This class is here to reduce repeated code across the entire codebase. Every XACML request that is created programmatically should be using this helper class.
 *
 * @author 
 * Justin Fleming - fleminjr@critical.com<br/>
 * Jeremy Fields - fieldsjd@critical.com
 */
public class RequestMaker {
	
	/**
	 * This method takes lists of key value pairs. Should always be supplying Subject, Action, and Resource. 
	 * Environments are optional, but an argument of null should be used in the case with no environments.
	 * 
	 * @param subjects - List of RequestMaker.Tuple objects corresponding to attributeId and value for a subject
	 * @param actions - List of RequestMaker.Tuple objects corresponding to attributeId and value for an action
	 * @param resources - List of RequestMaker.Tuple objects corresponding to attributeId and value for a resource
	 * @param environments - List of RequestMaker.Tuple objects corresponding to attributeId and value for an environment
	 * @return
	 */
	public String createRequest(List<Tuple<String,?>> subjects, List<Tuple<String,?>> actions, List<Tuple<String,?>> resources, List<Tuple<String,?>> environments){
		Document doc = DocumentHelper.createDocument();
		Namespace ns = new Namespace("",CTIConstants.XACML3_XMLNS);
		
		Element req = DocumentHelper.createElement(new QName("Request",ns));
		req.addAttribute("CombinedDecision", "false");
		req.addAttribute("ReturnPolicyIdList", "true");
		
		doc.add(req);
		
		Element sub = req.addElement("Attributes");
		sub.addAttribute("Category", CTIConstants.SUBJECT_CATEGORY);
		addAtts(sub, subjects);
		
		Element act = req.addElement("Attributes");
		act.addAttribute("Category", CTIConstants.ACTION_CATEGORY);
		addAtts(act, actions);
		
		Element res = req.addElement("Attributes");
		res.addAttribute("Category", CTIConstants.RESOURCE_CATEGORY);
		addAtts(res, resources);
		
		if(environments!=null){
			Element env = req.addElement("Attributes");
			env.addAttribute("Category", CTIConstants.ENVIRONMENT_CATEGORY);
			addAtts(env, environments);
		}
		
		return doc.asXML();

	}
	
	
	/**
	 * Helper method to loop over each Tuple object and add the appropriate XML tags to our XACML Request
	 * @param sar - Element object that the List of Tuples corresponds to
	 * @param stuff - List of Tuples of attributeId's and values
	 */
	private void addAtts(Element sar, List<Tuple<String,?>> stuff){			
		for (Tuple<String,?> t: stuff){
			if(t.y instanceof String){
				sar.addElement("Attribute").addAttribute("AttributeId",(String)t.x).addAttribute("IncludeInResult", "true")
					.addElement("AttributeValue").addAttribute("DataType", CTIConstants.STRING).addText((String)t.y);
			}else if(t.y instanceof ArrayList){
				//do stuff
				Element e = sar.addElement("Attribute").addAttribute("AttributeId",(String)t.x).addAttribute("IncludeInResult", "true");
				@SuppressWarnings("unchecked")
				ArrayList<String> list = (ArrayList<String>)t.y;
				Iterator<String> it = list.iterator();
				while(it.hasNext()){
					e.addElement("AttributeValue").addAttribute("DataType", CTIConstants.STRING).addText((String)it.next());
				}
			}else{
				//fail
				continue;
			}
		}
	}
	
	/**
	 * @author jrfleming3
	 * 
	 * Class for a simplistic key-value pair object.
	 * 
	 * @param <X>
	 * @param <Y>
	 */
	public class Tuple<X, Y> { 
		  public final X x; 
		  public final Y y; 
		  public Tuple(X x, Y y) { 
		    this.x = x; 
		    this.y = y; 
		  } 
	}
}



package critical;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;

import critical.GUIComp.ButtonRow;
import critical.GUIComp.ComboBoxInputRow;
import critical.GUIComp.TextInputRow;
import critical.GUIComp.extras;


public class GUI extends JFrame{

	private static final long serialVersionUID = 1L;
	private static final String[] comboOpts = {"None", "Authentication", "Attestation", "Auth+Attest"};
	static final String[] buttonLabels = {"Remove", "Upload", "Save", "Preview", "Exit"};
	static final String[] extraOpts = {"lat-long-in-polygon", "lat-long-not-in-polygon", "ip-on-whitelist", "ip-not-on-blacklist","ua-browser-type",
	"ua-browser-manufacturer", "ua-browser-name", "ua-browser-render-engine", "ua-browser-version", "ua-os-manufacturer", "ua-os-name", "ua-device-type",
	"ip-city", "ip-lat-long", "ip-country", "ip-region", "ip-timezone", "ip-zipcode", "pcr17"
	};
	static JFrame main;
	static GUIComp.ComboBoxInputRow aa;
	static GUIComp.TextInputRow resRow;
	static GUIComp.TextInputRow loc;
	static GUIComp.TextInputRow secPropRow;
	static GUIComp.TextInputRow authUserRow;
	static GUIComp.ButtonRow buttons;
	static GUIComp.extras ext;
	
	
	public static void main(String[] args) {
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });

	}
	
	private static JFrame initUI(){
		JFrame main = new JFrame("CTI - Policy Builder");
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.setLayout(new GridBagLayout());
		main.setResizable(false);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {} // doesn't matter, then it just defaults to java's "look and feel"
		return main;
	}
	
	
	private static void imlazy(){
		GUIUtils.guiObj.authusers.add("user1");
		GUIUtils.guiObj.authusers.add("user2");
		GUIUtils.guiObj.locations.add("loc1");
		GUIUtils.guiObj.locations.add("loc2");
		GUIUtils.guiObj.resources.add("res1");
		GUIUtils.guiObj.resources.add("res2");
		GUIUtils.guiObj.secprops.add("prop1");
		GUIUtils.guiObj.secprops.add("prop2");
		
		ArrayList<String> tmp = new ArrayList<String>();
		tmp.add("13029\n123456\n90210");
		GUIUtils.guiObj.extras.put("ip-zipcode", tmp);
		
		ArrayList<String> tmp1 = new ArrayList<String>();
		tmp1.add("1,1\n2,2\n3,3\n4,4");
		GUIUtils.guiObj.extras.put("lat-long-in-polygon", tmp1);
		
		ArrayList<String> tmp2 = new ArrayList<String>();
		tmp2.add("192.168.1.0/24\n172.20.5.3/16");
		GUIUtils.guiObj.extras.put("ip-on-whitelist", tmp2);
	}
	
	public static void createAndShowGUI(){
		main = initUI();
		imlazy();
		
		GridBagConstraints c = new GridBagConstraints();
		
		aa = new ComboBoxInputRow("Select Security Model", comboOpts);
		resRow = new TextInputRow("Enter Resource", "resource");
		loc = new TextInputRow("Enter ResLocation", "location");
		secPropRow = new TextInputRow("Enter SecProp", "secprop");
		authUserRow = new TextInputRow("Enter User", "email");
		ext = new extras(extraOpts);
		buttons = new ButtonRow(buttonLabels);
		
		authUserRow.disable();
		secPropRow.disable();
		
		c.fill = GridBagConstraints.VERTICAL; // everything is vertical to each other
		c.gridx = 0; // everything is in the same "column"
		
		c.anchor = GridBagConstraints.WEST; // left aligned
		c.gridy = 0; // row 0
		c.ipadx = 20;
		main.add(aa.getPanel(), c);
		
		c.gridy = 1; // row 1
		c.ipadx = 80;
		main.add(resRow.getPanel(), c);
		
		c.gridy = 2;
		main.add(loc.getPanel(), c);
		
		c.gridy = 3; // row 3
		main.add(secPropRow.getPanel(), c);

		c.gridy = 4; // row 4
		main.add(authUserRow.getPanel(), c);
		
		// Blank spaces
		c.anchor = GridBagConstraints.EAST;
		c.gridy = 5;
		main.add(new JLabel("         "),c);
		
		c.anchor = GridBagConstraints.WEST;
		c.gridy = 6;
		main.add(ext.getPanel(), c);
		
		
		c.anchor = GridBagConstraints.EAST;
		c.gridy = 7;
		main.add(new JLabel("          "),c);
		// end blank spaces
		
		c.gridy = 8; // row 5
		c.ipadx = 0;
		main.add(buttons.getPanel(), c);
				
		main.pack();
		main.setLocationRelativeTo(null);
		main.setVisible(true);
	}
	

}

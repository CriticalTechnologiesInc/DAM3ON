package critical;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class GUIComp {
	
	static class TextInputRow{
		private JPanel row = new JPanel();
		private JTextField tf = new JTextField(25);
		private JLabel hidden = new JLabel("", JLabel.RIGHT);
		private JButton b = new JButton("Add");
		
		
		TextInputRow(String label, String command){
			row.setLayout(new FlowLayout());
			
			JLabel labelText = new JLabel(label, JLabel.CENTER);
			labelText.setPreferredSize(new Dimension(90,26));
			
			b.setActionCommand(command);
			b.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
			         String command = e.getActionCommand(); 
			         
			         if(tf.getText().isEmpty())
			        	 return;
			         
			         hidden.setText("Added!");
			         
			         Timer timerFadeOut = fadeout(hidden);
			         ActionListener actionFadeIn = new ActionListener()
			         {   
			             @Override
			             public void actionPerformed(ActionEvent event)
			             {
			            	 Color c = hidden.getForeground();
			            	 if (c.getAlpha() < 255){
			            		 c = new Color(0,153,0,c.getAlpha() + 1);
			     				hidden.setForeground(c);
			     				hidden.repaint();
			     			}else{
			     				((Timer) event.getSource()).stop();
			     				timerFadeOut.start();
			     			}
			             }
			         };

			        Timer timerFadeIn = new Timer(2, actionFadeIn);
			        timerFadeIn.start();
			
			         if( command.equals("resource"))  {
			        	 GUIUtils.guiObj.resources.add(tf.getText());
			        	 tf.setText("");
			         } 	else if (command.equals("secprop")){
			        	 GUIUtils.guiObj.secprops.add(tf.getText());
			        	 tf.setText("");
			         } else if (command.equals("email")){
			        	 GUIUtils.guiObj.authusers.add(tf.getText());
			        	 tf.setText("");
			         } else if (command.equals("location")){
			        	 GUIUtils.guiObj.locations.add(tf.getText());
			        	 tf.setText("");
			         }
			      }
			});

			hidden.setPreferredSize(new Dimension(50,10));
			hidden.setForeground(new Color(0,0,0,0));
			

			labelText.setHorizontalAlignment(SwingConstants.LEFT);
			row.add(labelText );
			row.add(tf);
			row.add(b);
			row.add(hidden);

		}
		
		JPanel getPanel(){return row;}
		JLabel getHiddenLabel(){return hidden;}
		String getTextInput(){return tf.getText();}
		void disable(){tf.setEditable(false);
		b.setEnabled(false);
		}
		void enable(){tf.setEditable(true);
		b.setEnabled(true);}

	}
	
	
	static class ComboBoxInputRow{
		private JPanel row = new JPanel();
		private static JComboBox<String> list;
		
		ComboBoxInputRow(String label, String[] opts){
			list = new JComboBox<String>(opts);
			list.setSelectedIndex(0);
			
			list.addItemListener(new ItemListener() {
		        public void itemStateChanged(ItemEvent e) {
		        	if (e.getStateChange() == ItemEvent.SELECTED) {
		                String item = (String) e.getItem();
		                if(item.equals("Authentication")){
		                	GUI.authUserRow.enable();
		                	GUI.secPropRow.disable();
		                	GUIUtils.guiObj.secprops.clear();
		                }else if(item.equals("Attestation")){
		                	GUI.secPropRow.enable();
		                	GUI.authUserRow.disable();
		                	GUIUtils.guiObj.authusers.clear();
		                }else if(item.equals("Auth+Attest")){
		                	GUI.secPropRow.enable();
		                	GUI.authUserRow.enable();
		                }else{
		                	GUI.authUserRow.disable();
		                	GUI.secPropRow.disable();
		                	GUIUtils.guiObj.authusers.clear();
		                	GUIUtils.guiObj.secprops.clear();
		                }
		                
		             }
		        }
		    });
			
			JLabel labelText = new JLabel(label, JLabel.RIGHT);
			row.add(labelText);
			row.add(list);
		}
		
		JPanel getPanel(){return row;}
		static String getSelection(){
			String tmp = (String)list.getSelectedItem();
			return tmp.toLowerCase();
			}
		
	}
	
	static class ButtonRow{
		private JPanel row = new JPanel();
		
		ButtonRow(String[] labels){
			for(int i = 0; i<labels.length; i++){
				JButton b = new JButton(labels[i]);
				b.setActionCommand(labels[i]);
				b.addActionListener(new BottomButtonClickListener());
				row.add(b);
			}
		}
		
		public JPanel getPanel(){return row;}
		
		
	}
	
	private static class BottomButtonClickListener implements ActionListener{
		 public void actionPerformed(ActionEvent e) {
	         String command = e.getActionCommand(); 

	         if(command.equals(GUI.buttonLabels[0])){
	        	 remove r = new remove();
	        	 JOptionPane.showMessageDialog(null, r.getPanel(), "Remove Entry", JOptionPane.PLAIN_MESSAGE);
	        	 
	         }else if(command.equals(GUI.buttonLabels[1])){
	        	 GUIUtils.guiObj.secmod = ComboBoxInputRow.getSelection();
	        	 if(!GUIUtils.validatePolicyInfo()){
	        		 JOptionPane.showMessageDialog(null, "Policy is missing required information! Refusing to upload.");
	        		 return;
	        	 }
	        	 int result = getDbLogin();
	        	 if(result != JOptionPane.OK_OPTION)
	        		 return;
	        	 boolean valid = GUIUtils.testDbConnection();
	        	 if(valid){
	        		 
	        		 boolean db_result = GUIUtils.uploadToDb();
	        		 if(db_result)
	        			 JOptionPane.showMessageDialog(null, "Success!");
	        	 }
	         }else if( command.equals(GUI.buttonLabels[2]))  {
	        	 GUIUtils.guiObj.secmod = ComboBoxInputRow.getSelection();
	        	 if(!GUIUtils.validatePolicyInfo()){
	        		 JOptionPane.showMessageDialog(null, "Policy is missing required information! Refusing to save.");
	        		 return;
	        	 }
	        	 JFileChooser fc = new JFileChooser();
	        	 File f = new File("policy.xml");
	        	 fc.setSelectedFile(f);
	        	 
	        	 if(System.getProperty("os.name").toLowerCase().contains("windows"))
	        		 fc.setCurrentDirectory(new File (System.getProperty("user.home") + System.getProperty("file.separator")+ "Desktop"));
	        	 else
	        		 fc.setCurrentDirectory(new File (System.getProperty("user.home") + System.getProperty("file.separator")));
	        	 
	        	 if(fc.showDialog(null, "Save") == JFileChooser.APPROVE_OPTION){
	        		 File selected = fc.getSelectedFile();
	        		 if(selected.exists())
	        			 if(JOptionPane.showConfirmDialog(null,  "Are you sure you want to overwrite the existing file?", "Confirm", JOptionPane.YES_NO_OPTION ) == JOptionPane.YES_OPTION){
	        				 
		        		 	GUIUtils.saveFile(fc.getSelectedFile(), GUIUtils.buildXacml(true));	 
	        			 }else{
	        				 return;
	        			 }
        			 else{
        				 GUIUtils.guiObj.secmod = ComboBoxInputRow.getSelection();
	        		 	GUIUtils.saveFile(fc.getSelectedFile(), GUIUtils.buildXacml(true));
        			 }
	        	 }
	            
	         } 	else if (command.equals(GUI.buttonLabels[3])){
	        	 GUIUtils.guiObj.secmod = ComboBoxInputRow.getSelection();
	        	 GUIUtils.showPreview();
	         }else if(command.equals(GUI.buttonLabels[4])){
	        	 if(JOptionPane.showConfirmDialog(null,  "Are you sure you want to quit?", "Confirm", JOptionPane.YES_NO_OPTION ) == JOptionPane.YES_OPTION)
	        		 System.exit(0);
	         }
	      }
	}

	
	static Timer fadeout(JLabel hidden){
	    ActionListener actionFadeOut = new ActionListener()
	    {   
	        @Override
	        public void actionPerformed(ActionEvent event)
	        {
	       	 Color c = hidden.getForeground();
	       	 if (c.getAlpha() > 0){
	       		 c = new Color(0,153,0,c.getAlpha() - 1);
					hidden.setForeground(c);
					hidden.repaint();
				}else{
					((Timer) event.getSource()).stop();
					hidden.setText("");
				}
	        }
	    };
	    return new Timer(2, actionFadeOut);
	}
	
	
	private static Preferences getPreferences(){
		return Preferences.userRoot().node(GUIComp.class.getName());
	}
	
	static int getDbLogin(){
	
		JPanel panel = new JPanel(new GridBagLayout());
		
		JTextField username_tf = new JTextField(25);
		JPasswordField password_tf = new JPasswordField(25);
		JTextField location_tf = new JTextField(25);
		JTextField table_tf = new JTextField(25);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.VERTICAL; // everything is vertical to each other

		c.gridy = 0; // row 0
		c.gridx = 0; // column 0
		panel.add(new JLabel("Username:"), c);
		c.gridx = 1; // col 1
		panel.add(username_tf,c);
		
		c.gridy = 1; // row 1
		c.gridx = 0; //column 0
		panel.add(new JLabel("Password:"), c);
		c.gridx = 1; // col 1
		panel.add(password_tf, c);
		
		c.gridy = 2;
		c.gridx = 0;
		panel.add(new JLabel("Loc+DB:"), c);
		c.gridx = 1;
		panel.add(location_tf, c);
		
		c.gridy = 3;
		c.gridx = 0;
		panel.add(new JLabel("Table:"), c);
		c.gridx = 1;
		panel.add(table_tf, c);
		
		Preferences prefs = getPreferences();
		String username = prefs.get("username", null);
		String location = prefs.get("location", null);
		String table = prefs.get("table", null);
		
		if(username!=null && location!=null && table!=null){
			username_tf.setText(username);
			location_tf.setText(location);
			table_tf.setText(table);
		}
		
		
		int result = JOptionPane.showConfirmDialog(null, panel, "MySQL DB Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		
		if(result == JOptionPane.OK_OPTION){
			GUIUtils.dbObj.username = username_tf.getText();
			GUIUtils.dbObj.password = password_tf.getPassword();
			GUIUtils.dbObj.location = location_tf.getText();
			GUIUtils.dbObj.table = table_tf.getText();
			
			prefs.put("username", username_tf.getText());
			prefs.put("location", location_tf.getText());
			prefs.put("table", table_tf.getText());
		}
		
		return result;
		
	}
	
	static class extras{
		
		JPanel panel = new JPanel(new GridBagLayout());
		private static JComboBox<String> list;
		private static String choice = null;
		private static HintTextField jta = new HintTextField("Enter each value on its own line");
		
		private JButton b = new JButton("Add");
		private JLabel hidden = new JLabel("", JLabel.RIGHT);

		
		extras(String[] opts){
			jta.setRows(5);
			jta.setColumns(25);
			jta.setFont(new Font("Arial", 0, 12));
			
			
			list = new JComboBox<String>(opts);
			list.setSelectedIndex(0);
			choice = list.getItemAt(0);
			list.addItemListener(new ItemListener() {
		        public void itemStateChanged(ItemEvent e) {
		        	if (e.getStateChange() == ItemEvent.SELECTED) {
		                choice = (String) e.getItem();}}});
			
			////////////////////////
			b.setActionCommand("add");
			
			b.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
			         if(jta.getText().isEmpty())
			        	 return;
			         
			         hidden.setText("Added!");
			         
			         Timer timerFadeOut = fadeout(hidden);
			         ActionListener actionFadeIn = new ActionListener()
			         {   
			             @Override
			             public void actionPerformed(ActionEvent event)
			             {
			            	 Color c = hidden.getForeground();
			            	 if (c.getAlpha() < 255){
			            		 c = new Color(0,153,0,c.getAlpha() + 1);
			     				hidden.setForeground(c);
			     				hidden.repaint();
			     			}else{
			     				((Timer) event.getSource()).stop();
			     				timerFadeOut.start();
			     			}
			             }
			         };

			        Timer timerFadeIn = new Timer(2, actionFadeIn);
			        timerFadeIn.start();
			
			        // add it to our internal object that we use to create the policy later
			        ArrayList<String> vals;
			        if(GUIUtils.guiObj.extras.get(choice) == null){
			        	vals = new ArrayList<String>();
			        	vals.add(jta.getActualText());
			        	GUIUtils.guiObj.extras.put(choice, vals);
			        }else{
			        	vals = GUIUtils.guiObj.extras.get(choice);
			        	vals.add(jta.getActualText());
				        GUIUtils.guiObj.extras.put(choice, vals);
			        }
			        jta.setText("");
			        jta.focusLost();
			      }
			});
			///////////////////////
			hidden.setPreferredSize(new Dimension(50,10));
			hidden.setForeground(new Color(0,0,0,0));
			
			// add them all nicely
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.WEST;
			c.gridy = 0; // row 0
			c.gridx = 0; // col 0
			panel.add(list, c);
			
			c.gridy = 1; // row 1
			c.gridx = 0; // col 0
			panel.add(jta, c);
			
			c.anchor = GridBagConstraints.NORTHEAST;
			c.insets = new Insets(0,32,0,0);
			c.gridx = 1; // col 1
			panel.add(b, c);
			
			c.anchor = GridBagConstraints.SOUTH;
			c.insets = new Insets(0,20,20,0);
			panel.add(hidden, c);
		}
		
		
		public static String getChoice(){return choice;}
		public JPanel getPanel(){return panel;}
		
	}
	
	
	static class remove{
		JPanel panel = new JPanel(new GridBagLayout());
		
		private static JComboBox<String> resources;
		private static JComboBox<String> secprops;
		private static JComboBox<String> authusers;
		private static JComboBox<String> locations;
		private static JComboBox<String> extras;
		
		private JButton res_r;
		private JButton sec_r;
		private JButton aut_r;
		private JButton loc_r;
		private JButton ext_r;
		
		private JLabel res_l = new JLabel("Resources");
		private JLabel sec_l = new JLabel("SecProps");
		private JLabel aut_l = new JLabel("Users");
		private JLabel loc_l = new JLabel("Locations");
		private JLabel ext_l = new JLabel("Extras");
		
		public JPanel getPanel(){return panel;}
		
		remove(){
			String [] ext = new String[GUIUtils.guiObj.extras.size()];
			Set<String> keys = GUIUtils.guiObj.extras.keySet();
			
			int i = 0;
			for(String tmp : keys){
				ext[i] = tmp;
				i++;
			}
			extras = new JComboBox<String>(ext);
			
			String[] res_tmp = new String[GUIUtils.guiObj.resources.size()];
			res_tmp = GUIUtils.guiObj.resources.toArray(res_tmp);
			resources = new JComboBox<String>(res_tmp);
			
			String[] sec_tmp = new String[GUIUtils.guiObj.secprops.size()];
			sec_tmp = GUIUtils.guiObj.secprops.toArray(sec_tmp);
			secprops = new JComboBox<String>(sec_tmp);

			String[] aut_tmp = new String[GUIUtils.guiObj.authusers.size()];
			aut_tmp = GUIUtils.guiObj.authusers.toArray(aut_tmp);
			authusers = new JComboBox<String>(aut_tmp);

			String[] loc_tmp = new String[GUIUtils.guiObj.locations.size()];
			loc_tmp = GUIUtils.guiObj.locations.toArray(loc_tmp);
			locations = new JComboBox<String>(loc_tmp);

			extras.setPreferredSize(new Dimension(125,21));
			resources.setPreferredSize(new Dimension(125,21));
			secprops.setPreferredSize(new Dimension(125,21));
			authusers.setPreferredSize(new Dimension(125,21));
			locations.setPreferredSize(new Dimension(125,21));
			
			ext_r = new JButton("Remove");
			res_r = new JButton("Remove");
			sec_r = new JButton("Remove");
			aut_r = new JButton("Remove");
			loc_r = new JButton("Remove");
			
			
			ext_r.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					String sel_key = (String) extras.getSelectedItem();
					extras.remove(extras.getSelectedIndex());
					GUIUtils.guiObj.extras.remove(sel_key);
					
					String [] ext = new String[GUIUtils.guiObj.extras.size()];
					Set<String> keys = GUIUtils.guiObj.extras.keySet();
					
					int i = 0;
					for(String tmp : keys){
						ext[i] = tmp;
						i++;
					}
					extras.setModel(new DefaultComboBoxModel<String>(ext));
			      }
			});
			
			res_r.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					String sel_key = (String) resources.getSelectedItem();
					resources.remove(resources.getSelectedIndex());
					GUIUtils.guiObj.resources.remove(GUIUtils.guiObj.resources.indexOf((String) sel_key));
					
					String[] res_tmp = new String[GUIUtils.guiObj.resources.size()];
					res_tmp = GUIUtils.guiObj.resources.toArray(res_tmp);
					resources.setModel(new DefaultComboBoxModel<String>(res_tmp));
			      }
			});
			
			sec_r.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					String sel_key = (String) secprops.getSelectedItem();
					secprops.remove(secprops.getSelectedIndex());
					GUIUtils.guiObj.secprops.remove(GUIUtils.guiObj.secprops.indexOf((String) sel_key));
					
					String[] sec_tmp = new String[GUIUtils.guiObj.secprops.size()];
					sec_tmp = GUIUtils.guiObj.secprops.toArray(sec_tmp);
					secprops.setModel(new DefaultComboBoxModel<String>(sec_tmp));
			      }
			});
			
			aut_r.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					String sel_key = (String) authusers.getSelectedItem();
					authusers.remove(authusers.getSelectedIndex());
					GUIUtils.guiObj.authusers.remove(GUIUtils.guiObj.authusers.indexOf((String) sel_key));
					
					String[] aut_tmp = new String[GUIUtils.guiObj.authusers.size()];
					aut_tmp = GUIUtils.guiObj.authusers.toArray(aut_tmp);
					authusers.setModel(new DefaultComboBoxModel<String>(aut_tmp));
			      }
			});
			
			loc_r.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					String sel_key = (String) locations.getSelectedItem();
					locations.remove(locations.getSelectedIndex());
					GUIUtils.guiObj.locations.remove(GUIUtils.guiObj.locations.indexOf((String) sel_key));
					
					String[] loc_tmp = new String[GUIUtils.guiObj.locations.size()];
					loc_tmp = GUIUtils.guiObj.locations.toArray(loc_tmp);
					locations.setModel(new DefaultComboBoxModel<String>(loc_tmp));
			      }
			});
			
			
			/// Style
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(0,10,5,0);
			c.gridy = 0; // row 0
			c.gridx = 0; // col 0
			c.anchor = GridBagConstraints.WEST;
			panel.add(ext_l, c);
			c.anchor = GridBagConstraints.CENTER;
			
			c.gridx = 1; // col 1
			panel.add(extras, c);
			
			c.gridx = 2; // col 2
			panel.add(ext_r, c);
			//-----------------------------
			c.gridy = 1;
			c.gridx = 0; // col 0
			c.anchor = GridBagConstraints.WEST;
			panel.add(res_l, c);
			c.anchor = GridBagConstraints.CENTER;
			
			c.gridx = 1; // col 1
			panel.add(resources, c);
			
			c.gridx = 2; // col 2
			panel.add(res_r, c);
			//-----------------------------
			c.gridy = 2;
			c.gridx = 0; // col 0
			c.anchor = GridBagConstraints.WEST;
			panel.add(sec_l, c);
			c.anchor = GridBagConstraints.CENTER;
			
			c.gridx = 1; // col 1
			panel.add(secprops, c);
			
			c.gridx = 2; // col 2
			panel.add(sec_r, c);
			//-----------------------------
			c.gridy = 3;
			c.gridx = 0; // col 0
			c.anchor = GridBagConstraints.WEST;
			panel.add(aut_l, c);
			c.anchor = GridBagConstraints.CENTER;
			
			c.gridx = 1; // col 1
			panel.add(authusers, c);
			
			c.gridx = 2; // col 2
			panel.add(aut_r, c);
			//-----------------------------
			c.gridy = 4;
			c.gridx = 0; // col 0
			c.anchor = GridBagConstraints.WEST;
			panel.add(loc_l, c);
			c.anchor = GridBagConstraints.CENTER;
			
			c.gridx = 1; // col 1
			
			panel.add(locations, c);
			
			c.gridx = 2; // col 2
			panel.add(loc_r, c);
		}
	}
	
	
}
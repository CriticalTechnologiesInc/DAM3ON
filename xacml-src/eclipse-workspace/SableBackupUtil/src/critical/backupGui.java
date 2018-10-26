package critical;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.UIManager;

import critical.Comps.MainButton;
import critical.Comps.DescLabel;;

public class backupGui {

	static JFrame main;

	public static void main(String[] args) {

		if (backupUtils.isUnix())
			backupUtils.setFortyCustomPath("/etc/grub.d/40_custom");
		else if (backupUtils.isWindows())
			backupUtils.setFortyCustomPath("c:\\users\\"+System.getProperty("user.name")+"\\desktop\\40_custom");
		else {
			Comps.simpleMessageWindow("Unsupported OS!");
			return;
		}
		
		if(GraphicsEnvironment.isHeadless()){
			doCommandLine();
		}else{
			javax.swing.SwingUtilities.invokeLater(new Runnable() {public void run() {createAndShowGUI();}});	
		}
		
	}

	static void doCommandLine(){
		if(backupUtils.backupFiles()){
			System.exit(0);
		}else{
			System.out.println("Some error occurred :(");
			System.exit(1);
		}
	}
	
	private static JFrame initUI() {

		JFrame main = new JFrame("CTI - Sable Binary Backup");
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.setLayout(new GridBagLayout());
		main.setResizable(false);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		} // doesn't matter, then it just defaults to java's "look and feel"
		return main;

	}

	private static void createAndShowGUI() {

		

		main = initUI();
		GridBagConstraints c = new GridBagConstraints();
		MainButton bigButton = new MainButton();
		DescLabel descLabel = new DescLabel("<html>Press button below to backup <br/> all SABLE measured binary files</html>");

		c.gridy = 0;
		main.add(descLabel.getLabel(), c);

		c.gridy = 1;
		main.add(Box.createVerticalStrut(40), c);

		c.gridy = 2;
		main.add(bigButton.getButton(), c);

		main.pack();
		main.setSize(new Dimension(350, 200));
		main.setLocationRelativeTo(null);
		main.setVisible(true);

	}

}

package critical;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Comps {

	private static String fontName = "Arial";

	static class MainButton {
		private JButton b = new JButton("Begin");

		MainButton() {
			b.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (backupUtils.backupFiles())
						System.exit(0);

				}
			});

			b.setPreferredSize(new Dimension(150, 75));

			b.setFont(new Font(fontName, Font.PLAIN, 20));

		}

		JButton getButton() {
			return b;
		}
	}

	static class DescLabel {
		private JLabel l;

		DescLabel(String description) {
			l = new JLabel(description);
			l.setFont(new Font(fontName, Font.PLAIN, 14));
		}

		JLabel getLabel() {
			return l;
		}

	}

	static void simpleMessageWindow(String message) {
		if (GraphicsEnvironment.isHeadless())
			System.out.println(message);
		else
			JOptionPane.showMessageDialog(null, message);
	}

	static boolean getSaveLocation(ArrayList<File> files) {
		if (GraphicsEnvironment.isHeadless()) {
			File selected = backupUtils.getSaveLocationFromUser();
			if(selected.exists()){
				if(!backupUtils.getYesNoFromStdin("File already exists. Overwrite? (y/n): ")){
					System.out.println("Please remove/rename " + selected.getName() + " and run this program again.");
					System.exit(2);
				}
			}
			try {
				backupUtils.compressFiles(files, selected);
				backupUtils.filename = selected.getAbsolutePath();
			} catch (IOException e) {
				System.out.println(e);
				return false;
			}
			return true;
		} else {
			JFileChooser fc = new JFileChooser();
			File f = new File("sable_backup" + new SimpleDateFormat("_MM-dd-yy").format(new Date()) + ".tar.gz");
			fc.setSelectedFile(f);

			if (System.getProperty("os.name").toLowerCase().contains("windows"))
				fc.setCurrentDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Desktop"));
			else
				fc.setCurrentDirectory(new File(System.getProperty("user.home") + System.getProperty("file.separator")));

			if (fc.showDialog(null, "Save") == JFileChooser.APPROVE_OPTION) {
				File selected = fc.getSelectedFile();

				if (selected.exists())
					if (JOptionPane.showConfirmDialog(null, "Are you sure you want to overwrite the existing file?", "Confirm",
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						try {
							backupUtils.compressFiles(files, selected);
							backupUtils.filename = selected.getAbsolutePath();
							return true;
						} catch (IOException e) {
							return false;
						}
					} else {
						backupUtils.filename = "cancel";
						return true;
					}
				else {
					try {
						backupUtils.compressFiles(files, selected);
						backupUtils.filename = selected.getAbsolutePath();
						return true;
					} catch (IOException e) {
						return false;
					}
				}
			} else {
				backupUtils.filename = "cancel";
				return true;
			}

		}
	}

}

package critical;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.*;
import org.apache.commons.io.IOUtils;

public class backupUtils {

	private static String fortyCustomPath;
	private static ArrayList<String> ml;
	private static ArrayList<File> fl;
	static String filename = null;

	static void setFortyCustomPath(String path){fortyCustomPath = path;}
	
	static boolean backupFiles() {
		if (!parse40Custom()) {
			Comps.simpleMessageWindow("Could not find the '40_custom' file in "+fortyCustomPath+" !");
			return false;
		}

		if (!convertStringToFiles()) {
			Comps.simpleMessageWindow("An entry in 40_custom does not exist as a file!");
			return false;
		}

		if (!Comps.getSaveLocation(fl)) {
			Comps.simpleMessageWindow("There was an error saving the backup file!");
			return false;
		}
		if (!(filename == null || filename.equals("cancel"))){
			Comps.simpleMessageWindow("Backup successfully saved to: " + filename + ". Make sure this file is not stored locally!!");
			return true;
		}
		return false;

	}
	
	static File getSaveLocationFromUser(){
		@SuppressWarnings("resource")
		Scanner stdin = new Scanner(System.in); // so stupid, if you close it, it closes it JVM wide, and can't be re-opened.
		boolean userIsDumb = true;
		File saveLocation = null;
		while(userIsDumb){
			System.out.print("Enter a location to store the backed-up files: ");
			String saveLocationStdin = stdin.nextLine();
			
			if(backupUtils.validDirectoryPath(saveLocationStdin)){
				if(saveLocationStdin.substring(saveLocationStdin.length() -1, saveLocationStdin.length()).equals(File.separator)){
					saveLocationStdin += "sable_backup" + new SimpleDateFormat("_MM-dd-yy").format(new Date()) + ".tar.gz";
				}else{
					saveLocationStdin += File.separator + "sable_backup" + new SimpleDateFormat("_MM-dd-yy").format(new Date()) + ".tar.gz";
				}
				saveLocation = new File(saveLocationStdin);
				userIsDumb = false;
			}
			else
				System.out.println("Not a valid directory path. Try again.");
		}
		return saveLocation;
	}

	private static boolean convertStringToFiles() {
		fl = new ArrayList<File>();
		for (String file : ml) {
			if (validFilePath(file))
				fl.add(new File(file));
			else
				return false;
		}
		return true;

	}

	/**
	 * Compress (tar.gz) the input files to the output file
	 *
	 * @param files
	 *            The files to compress
	 * @param output
	 *            The resulting output file (should end in .tar.gz)
	 * @throws IOException
	 */
	public static void compressFiles(ArrayList<File> files, File output) throws IOException {
		// Create the output stream for the output file
		FileOutputStream fos = new FileOutputStream(output);
		// Wrap the output file stream in streams that will tar and gzip everything
		TarArchiveOutputStream taos = new TarArchiveOutputStream(new GZIPOutputStream(new BufferedOutputStream(fos)));

		// TAR has an 8 gig file limit by default, this gets around that
		taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR); // to get past the 8 gig limit
		// TAR originally didn't support long file names, so enable the support for it
		taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

		// Get to putting all the files in the compressed output file
		for (File f : files) {
			addFilesToCompression(taos, f, f.getParent());
		}

		// Close everything up
		taos.close();
		fos.close();
	}

	/**
	 * Does the work of compression and going recursive for nested directories &lt;p/&gt;
	 *
	 * Borrowed heavily from http://www.thoughtspark.org/node/53
	 *
	 * @param taos
	 *            The archive
	 * @param file
	 *            The file to add to the archive
	 * @param dir
	 *            The directory that should serve as the parent directory in the archivew
	 * @throws IOException
	 */
	private static void addFilesToCompression(TarArchiveOutputStream taos, File file, String dir) throws IOException {
		// Create an entry for the file
		TarArchiveEntry tae = new TarArchiveEntry(file, dir + File.separator + file.getName());
		if (isUnix()) {
			int mode = (Integer) Files.getAttribute(file.toPath(), "unix:mode", LinkOption.NOFOLLOW_LINKS);
			int uid = (Integer) Files.getAttribute(file.toPath(), "unix:uid", LinkOption.NOFOLLOW_LINKS);
			int gid = (Integer) Files.getAttribute(file.toPath(), "unix:gid", LinkOption.NOFOLLOW_LINKS);

			tae.setMode(mode);
			tae.setUserId(uid);
			tae.setGroupId(gid);
		}

		taos.putArchiveEntry(tae);
		if (file.isFile()) {
			// Add the file to the archive
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			IOUtils.copy(bis, taos);
			taos.closeArchiveEntry();
			bis.close();
		} else if (file.isDirectory()) {
			// close the archive entry
			taos.closeArchiveEntry();
			// go through all the files in the directory and using recursion, add them to the archive
			for (File childFile : file.listFiles()) {
				addFilesToCompression(taos, childFile, file.getName());
			}
		}
	}

	private static boolean parse40Custom() {
		if (!validFilePath(fortyCustomPath))
			return false;

		String fc = readFile(fortyCustomPath);
		String[] lines = fc.split("\n");

		boolean flag = false;
		ml = new ArrayList<String>();

		for (String tmp : lines) {
			if (tmp.toLowerCase().contains("measure linux")) {
				flag = true;
				continue;
			}
			if (tmp.contains("}") && flag) {
				if (!(tmp.replace("}", "").trim().equals("")))
					ml.add(tmp.replace("module", "").trim().replace("}", ""));
				break;
			}
			if (flag && !tmp.toLowerCase().contains("multiboot"))
				ml.add(tmp.replace("module", "").trim());
		}
		return true;
	}

	/**
	 * This method reads a file at a given path and returns the contents of the file as a String
	 * 
	 * @param path
	 *            full path of file to read
	 * @return String of file contents
	 * @throws IOException
	 */
	public static String readFile(String path) {
		byte[] encoded = null;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
			return new String(encoded, StandardCharsets.UTF_8);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Description: Checks whether a given path is valid & to a file.
	 * 
	 * @param path
	 *            String of a path to a file
	 * @return boolean Valid or not
	 */
	public static boolean validFilePath(String path) {
		if (path == null || path.isEmpty())
			return false;
		File f = new File(path);
		return f.exists() && f.isFile();
	}

	public static boolean isWindows() {
		return (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0);
	}

	public static boolean isUnix() {
		String tmp = System.getProperty("os.name").toLowerCase();
		return (tmp.indexOf("nix") >= 0 || tmp.toLowerCase().indexOf("nux") >= 0 || tmp.toLowerCase().indexOf("aix") > 0);
	}
	
	/**
	 * Description:
	 * Checks whether a given path is valid & to a directory
	 * 
	 * @param path String of a path to a directory
	 * @return boolean Valid or not
	 */
	public static boolean validDirectoryPath(String path){
		if(path == null || path.isEmpty())
			return false;
		File f = new File(path);
		return f.exists() && f.isDirectory();
	}
	
	public static boolean getYesNoFromStdin(String message){
		@SuppressWarnings("resource")
		Scanner stdin = new Scanner(System.in);
		boolean userIsDumb = true;
		boolean result = false;
		
		while(userIsDumb){
			System.out.print(message);
			String yn = stdin.nextLine();
			yn = yn.toLowerCase();
			
			
			if(yn.equals("y") || yn.equals("n")){
				if(yn.equals("y"))
					result = true;
				userIsDumb = false;
			}
			else
				System.out.println("Not a valid answer. Try again.");
		}
//		stdin.close();
		return result;
	
	}

}

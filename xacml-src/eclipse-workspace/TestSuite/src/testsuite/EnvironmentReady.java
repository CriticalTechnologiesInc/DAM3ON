package testsuite;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Scanner;

import core.Utils;
/**
 * 
 * @author Jeremy
 * this is kind of lame. not sure if it's even worth it?
 * if we ever make an installer package, we'd want to do something like this.
 * 
 * Should
 * - check versions of programs;
 * - let user choose what they're installing (pdp? pep? multi-login? etc.) and report based on that
 */


public class EnvironmentReady {

	private static final String usr_bin = "/usr/bin/";
	private static final String usr_local_src = "/usr/local/src/";
	private static final String usr_local_bin = "/usr/local/bin/";
	private static final String usr_sbin = "/usr/sbin/";
	
	private static final String[] pepPrograms = {"apache2", "git", "git-annex", "python", "java"};
	private static final String[] pdpPrograms = {"apache2", "git", "git-annex", "python", "java", "tpm_verifyquote", "gpg"};
	
	private static final int numDots = 3;
	private static final int dotDelay = 500;
	
	private static final int PEP = 1;
	private static final int PDP = 2;
	
	public static void main(String[] args) {
		int ent = whichEntity();
		
		/* operating system */
		System.out.print("Checking Operating System");
		Utils.loadingDots(numDots, dotDelay);
		if(Utils.isUnix() || Utils.isWindows())
			System.out.println("success");
		else{
			System.out.println("fail - *nix required");
			System.exit(1);
		}
		
		/* common programs */
		System.out.println("-------------------------------");
		System.out.println("Checking installed programs");
		System.out.println("apache-tomcat in "+usr_local_src+"... " + doesDirectoryNameExist(usr_local_src, "apache-tomcat"));
		
		String[] programs = null;
		if(ent == PEP)
			programs = pepPrograms;
		else if (ent == PDP)
			programs = pdpPrograms;
		else
			System.exit(2);
		
		for(String program: programs){
			System.out.print(program);
			Utils.loadingDots(numDots, dotDelay);
			System.out.println(doesProgramExist(program));
		}
		
		/* directories */
		System.out.println("-------------------------------");
		System.out.println("Checking directories...");
		
		System.out.print("/etc/webxacml/ exists");
		Utils.loadingDots(numDots, dotDelay);
		System.out.println(Utils.validDirectoryPath("/etc/webxacml/"));
		
		
	}
	
	private static boolean doesDirectoryNameExist(String path, String name){
		File file = new File(path);
		String[] directories = file.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		return Arrays.toString(directories).toLowerCase().contains(name.toLowerCase());
	}
	
	private static boolean doesProgramExist(String p_name){
		if (p_name == null || p_name.isEmpty())
			return false;
		else
			return (new File(usr_bin+p_name).isFile() || new File(usr_local_bin+p_name).isFile() || new File(usr_sbin+p_name).isFile() );
	}
	
	
	private static int whichEntity(){
		System.out.println("Which entity is this system?");
		System.out.println("(1) PEP\n(2) PDP");
		System.out.print("Choice: ");
		
		Scanner s = new Scanner(System.in);

		while(true){
			try{
				int choice = s.nextInt();
				if(choice == 1 || choice == 2){
					s.close();
					return choice;
				}else{
					System.out.println("Bad input. Try again: ");
					System.out.print("Choice: ");
					s.nextLine();
				}
			}catch(Exception e){
				System.out.println("Bad input. Try again: ");
				System.out.print("Choice: ");
				s.nextLine();
			}
		}
	
	}
}

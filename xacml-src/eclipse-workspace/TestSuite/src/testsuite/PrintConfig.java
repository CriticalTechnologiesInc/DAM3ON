package testsuite;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import testsuite.Config;

/**
 * @author Jeremy Fields - fieldsjd@critical.com
 *
 * This program will recursively print out all 'public static final' fields
 * of all sub classes of the Config class.
 *
 */
public class PrintConfig {
	private static final String classDelim = "--------------------------";
	private static final String delim = " -----> ";
	private static final String tab = "\t";
	
	public static void main(String args[]) throws IllegalArgumentException, IllegalAccessException {
		
		String confPath = Utilities.getConfigPathFromUser();

		Class<?> c = new Config(confPath).getClass();
		System.out.println(c.getName());
		printVars(c,null);
	}

	/**
	 * This takes in a <?> Class, and a 'level'.
	 * Then we get all declared fields and sub classes of the given class. For each field, if it's "public static final",
	 * we print it using "level" for formatting.
	 * 
	 * For each sub class in the given class, we recurse.
	 * 
	 * @param c Class object of the class to print all fields for
	 * @param level String of a formatter/delimiter? Just start with null and it will take care of it, adding to it for each recursion level.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static void printVars(Class<?> c, String level) throws IllegalArgumentException, IllegalAccessException{
		Class<?>[] classes = c.getClasses();
		Field[] fields = c.getDeclaredFields();
		
		if(level == null)
			level = tab;

		for(Field field : fields){
			if(Modifier.toString(field.getModifiers()).equals("public static final")){
				try{
					System.out.println(level + tab + field.getName() + delim + field.get(null).toString());
				}catch(NullPointerException e){
					System.out.println(level + tab + field.getName() + delim + "NULL!");
				}
			}
		}
		
		for(Class<?> c_tmp : classes){
			System.out.println(level + c_tmp.getSimpleName() + classDelim);
			printVars(c_tmp,level + tab);
		}
	}

}

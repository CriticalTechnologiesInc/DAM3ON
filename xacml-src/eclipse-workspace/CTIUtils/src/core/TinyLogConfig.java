package core;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.Writer;

/**
 * This class has functions to help with the configuration of TinyLogger
 * @author 
 * Justin Fleming - fleminjr@critical.com
 * Jeremy Fields - fieldsjd@critical.com
 * 
 */
public class TinyLogConfig {


	
	public static Writer wr;
	/**
	 * This method sets the logger to the normal config settings (based on debug value in config.properties)
	 */
	public static void config(){
		boolean DEBUG = Config.Basic.debug;
		
		Configurator.currentConfig().maxStackTraceElements(50);
		wr = (Writer) new org.pmw.tinylog.writers.FileWriter(Config.Basic.log,true,true);
		
		if(DEBUG){
			Configurator.currentConfig().writer(wr)
				.level(org.pmw.tinylog.Level.DEBUG)
				.formatPattern("{level}:\t{message}")
				.activate();
		}else{
			Configurator.currentConfig().writer(wr)
				.level(org.pmw.tinylog.Level.INFO)
				.formatPattern("{level}:\t{message}")
				.activate();
		}
	}
	
	/**
	 * This method is a convenience function that logs a newline to make the log file more readable. It then sets the logger back to the default settings. 
	 */
	public static void logNewLine(){
		Configurator.currentConfig().formatPattern("{message}").activate();
		Logger.info("");
		Configurator.currentConfig().formatPattern("{level}:\t{message}").activate();

	}
	
	public static void killTinyLog(){
//		Configurator.currentConfig().shutdownConfigurationObserver(false);
//		Configurator.currentConfig().shutdownWritingThread(false);
//		Configurator.currentConfig().removeAllWriters();
//		Configurator.currentConfig().removeWriter(wr);
		try {
			wr.close();
		} catch (Exception e) {

		}
	}
}
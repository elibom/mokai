package org.mokai.boot;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Boots the gateway using Spring configuration files.
 * 
 * @author German Escobar
 */
public class Main {

	public static void main(String[] args) {

		// start spring context
		String[] configLocations = new String[] { 
				"conf/core-context.xml", "conf/admin-console-context.xml" 
			};
		
		final ConfigurableApplicationContext springContext = new FileSystemXmlApplicationContext(configLocations);
		
		// add a shutdown hook to close spring context
		Runtime.getRuntime().addShutdownHook(new Thread(){
		    public void run() {
		    	springContext.close();
		    }
		});
	}

}

package org.mokai.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Boots the gateway using Spring configuration files.
 *
 * @author German Escobar
 */
public final class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);

    /**
     * This class shouldn't be instantiated.
     */
    private Main() {
    }

    public static void main(String[] args) {
        // start spring context
        System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "*");

        String[] configLocations = new String[]{
            "classpath*:core-context.xml", "classpath*:jogger-context.xml"
        };

        final ConfigurableApplicationContext springContext = new FileSystemXmlApplicationContext(configLocations);

        // add a shutdown hook to close spring context
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                log.info("stopping spring context ... ");
                springContext.stop();
                springContext.close();
                log.info("<< spring context stopped >>");
            }
        });
    }

}

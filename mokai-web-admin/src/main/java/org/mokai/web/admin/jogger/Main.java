package org.mokai.web.admin.jogger;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author Alejandro <lariverosc@gmail.com>
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "*");
            String[] contextLocations = {
                "classpath*:core-context.xml",
                "classpath*:jogger-context.xml"
            };
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(contextLocations);
            context.registerShutdownHook();
        } catch (Throwable e) {
            log.error("::*********:: FATAL ERROR - MOKAI ::********::", e);
        }
    }

}

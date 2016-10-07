package org.mokai.web.admin.jogger;

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
            String[] contextLocations = {
                "conf/core-context.xml",
                "conf/jogger-context.xml",
                "conf/admin-console-context.xml",
            };
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(contextLocations);
            context.registerShutdownHook();
        } catch (Throwable e) {
            log.error("::*********:: FATAL ERROR - MOKAI ::********::", e);
        }
    }

}

package org.mokai.web.admin.jogger.controllers;

import com.elibom.jogger.Jogger;
import com.elibom.jogger.test.JoggerTest;
import org.mokai.web.admin.jogger.MokaiJoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.BeforeSuite;

public class MokaiTest extends JoggerTest {

    private ApplicationContext springContext;

    private static Jogger jogger;

    @BeforeSuite
    public void doInit() throws Exception {
        System.setProperty("CODEBASE_ENV", "test");
        System.setProperty("BASEDIR", "../codebase-web");
        String[] configLocations = new String[]{"src/test/spring/test-context.xml", "../mokai-boot/conf/jogger-context.xml"};
        springContext = new ClassPathXmlApplicationContext(configLocations);
        jogger = springContext.getBean("joggerFactory", MokaiJoggerFactory.class).create();
    }

    protected ApplicationContext getSpringContext() {
        return springContext;
    }

    @Override
    protected Jogger getJogger() throws Exception {
        return jogger;
    }

}

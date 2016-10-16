package org.mokai.web.admin.jogger.controllers;

import com.elibom.jogger.Jogger;
import com.elibom.jogger.test.JoggerTest;
import java.util.UUID;
import org.mokai.web.admin.jogger.MokaiJoggerFactory;
import org.mokai.web.admin.jogger.Session;
import org.mokai.web.admin.jogger.SessionsManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class MokaiTest extends JoggerTest {

    private ClassPathXmlApplicationContext applicationContext;

    private static Jogger jogger;

    @BeforeSuite
    public void doInit() throws Exception {
        System.setProperty("CODEBASE_ENV", "test");
        System.setProperty("BASEDIR", ".");
        String[] configLocations = new String[]{"classpath*:jogger-context.xml", "classpath*:test-context.xml"};
        applicationContext = new ClassPathXmlApplicationContext(configLocations);
        jogger = applicationContext.getBean("joggerFactory", MokaiJoggerFactory.class).create();
    }

    @AfterSuite
    public void doDestroy() {
        applicationContext.close();
    }
    protected ApplicationContext getSpringContext() {
        return applicationContext;
    }

    @Override
    protected Jogger getJogger() throws Exception {
        return jogger;
    }

    protected String createSession(){
        SessionsManager sessionsManager = applicationContext.getBean("sessionsManager",SessionsManager.class);
        Session session = new Session();
        sessionsManager.addSession(session);
        return session.getId();
    }

}

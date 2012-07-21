package org.mokai.web.admin.jogger.controllers;

import org.jogger.config.Interceptors;
import org.jogger.test.MockJoggerServlet;
import org.jogger.test.spring.SpringJoggerTest;
import org.mokai.web.admin.jogger.interceptors.AppInterceptors;

public class AbstractTest extends SpringJoggerTest {

	@Override
	protected String[] getConfigLocations() {
		return new String[] {"src/test/spring/test-context.xml", "../mokai-boot/conf/jogger-context.xml"};
	}

	@Override
	protected Interceptors getInterceptors() {
		
		AppInterceptors interceptors = new AppInterceptors();
		interceptors.setApplicationContext( getSpringContext() );
		interceptors.initialize();
		
		return interceptors;
		
	}

	@Override
	protected String getRoutesPath() {
		return "src/main/resources/webapp/WEB-INF/routes.config";
	}

	@Override
	protected MockJoggerServlet getJoggerServlet() {
		MockJoggerServlet joggerServlet = new MockJoggerServlet();
		joggerServlet.setInterceptors( getInterceptors() );
		joggerServlet.setTemplatesPath("src/main/resources/webapp/WEB-INF/freemarker");
		
		return joggerServlet;
	}

}

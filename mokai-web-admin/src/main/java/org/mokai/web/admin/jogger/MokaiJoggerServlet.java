package org.mokai.web.admin.jogger;

import org.jogger.JoggerServlet;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

/**
 * Extends the JoggerServlet to set the Spring ApplicationContext into the ServletContext (this is what Spring
 * actually does when you bootstrap from a Servlet environment).
 *
 * @author German Escobar
 */
public class MokaiJoggerServlet extends JoggerServlet implements ApplicationContextAware {

	private static final long serialVersionUID = 1L;

	private ApplicationContext applicationContext;

	@Override
	protected void doInit() {
		WebApplicationContext webApplicationContext = new GenericWebApplicationContext(
				 new DefaultListableBeanFactory(applicationContext), getServletContext());

		getServletContext().setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
				webApplicationContext);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}


}

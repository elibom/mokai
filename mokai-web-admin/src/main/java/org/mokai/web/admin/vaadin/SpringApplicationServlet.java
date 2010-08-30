package org.mokai.web.admin.vaadin;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.ApplicationServlet;

public class SpringApplicationServlet extends ApplicationServlet implements ApplicationContextAware {

	private static final long serialVersionUID = 1L;
	
	private ApplicationContext applicationContext;

    /**
     * Get the {@link AutowireCapableBeanFactory} associated with the containing Spring {@link WebApplicationContext}.
     * This only works after the servlet has been initialized (via {@link #init init()}).
     *
     * @throws ServletException if the operation fails
     */
    protected final AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws ServletException {
        try {
            return applicationContext.getAutowireCapableBeanFactory();
        } catch (IllegalStateException e) {
            throw new ServletException("containing context " + applicationContext + " is not autowire-capable", e);
        }
    }

    /**
     * Create and configure a new instance of the configured application class.
     *
     * <p>
     * The implementation in {@link AutowiringApplicationServlet} delegates to
     * {@link #getAutowireCapableBeanFactory getAutowireCapableBeanFactory()}, then invokes
     * {@link AutowireCapableBeanFactory#createBean AutowireCapableBeanFactory.createBean()}
     * using the configured {@link Application} class.
     * </p>
     *
     * @param request the triggering {@link HttpServletRequest}
     * @throws ServletException if creation or autowiring fails
     */
    @Override
    protected Application getNewApplication(HttpServletRequest request) throws ServletException {
        
    	Class<? extends Application> cl = null;
    	
    	try {
    		cl = getApplicationClass();
    	} catch (ClassNotFoundException e) {
    		throw new ServletException("application class couldn't be found: " + e.getMessage(), e);
    	}
    	
    	try {
        	AutowireCapableBeanFactory beanFactory = getAutowireCapableBeanFactory();
            return beanFactory.createBean(cl);
        } catch (BeansException e) {
            throw new ServletException("failed to create new instance of " + cl, e);
        }
    }

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
}

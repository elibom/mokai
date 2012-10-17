package org.mokai.web.admin.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mokai.ConnectorService;
import org.mokai.RoutingEngine;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * A servlet that will print a string with the processors and receivers, and their corresponding status and state.
 * It is built to monitor the gateway using a service like Alertra (http://www.alertra.com/) o SiteUptime 
 * (http://siteuptime.com). 
 * 
 * @author German Escobar
 */
public class MonitoringServlet extends HttpServlet implements ApplicationContextAware {
	
	/**
	 * Generated Serial Version UID.
	 */
	private static final long serialVersionUID = 8295955912876921471L;
	
	private ApplicationContext applicationContext;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter out = resp.getWriter();
	
		StringBuffer status = new StringBuffer(); // this is what we will return
		
		RoutingEngine routingEngine = (RoutingEngine) applicationContext.getBean("routingEngine");
		
		for (ConnectorService application : routingEngine.getApplications()) {
			status.append(application.getId())
					.append("_")
					.append(application.getStatus())
					.append("_")
					.append(application.getState())
					.append(" ");
		}
		
		for (ConnectorService connection : routingEngine.getConnections()) {
			status.append(connection.getId())
					.append("_")
					.append(connection.getStatus())
					.append("_")
					.append(connection.getState())
					.append(" ");
		}
		
		out.println(status.toString());
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}

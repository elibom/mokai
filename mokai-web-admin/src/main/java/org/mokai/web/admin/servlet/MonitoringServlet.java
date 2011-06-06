package org.mokai.web.admin.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mokai.ProcessorService;
import org.mokai.ReceiverService;
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
	
		String status = ""; // this is what we will return
		
		RoutingEngine routingEngine = (RoutingEngine) applicationContext.getBean("routingEngine");
		
		for (ProcessorService processor : routingEngine.getProcessors()) {
			status += processor.getId() + "_" + processor.getStatus() + "_" + processor.getState() + " ";
		}
		
		for (ReceiverService receiver : routingEngine.getReceivers()) {
			status += receiver.getId() + "_" + receiver.getStatus() + "_" + receiver.getState() + " ";
		}
		
		out.println(status);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}

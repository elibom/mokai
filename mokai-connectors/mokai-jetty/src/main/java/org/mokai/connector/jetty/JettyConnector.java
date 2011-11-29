package org.mokai.connector.jetty;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Map;

import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.security.Credential;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.MappedLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.mokai.Configurable;
import org.mokai.Connector;
import org.mokai.ExposableConfiguration;
import org.mokai.Message;
import org.mokai.MessageProducer;
import org.mokai.Serviceable;
import org.mokai.annotation.Description;
import org.mokai.annotation.Name;
import org.mokai.annotation.Resource;

/**
 * 
 * @author German Escobar
 */
@Name("Jetty")
@Description("Receives messages through HTTP")
public class JettyConnector implements Connector, Configurable, Serviceable, ExposableConfiguration<JettyConfiguration> {
	
	@Resource
	private MessageProducer messageProducer;
	
	private JettyConfiguration configuration;
	
	private Server server;
	
	public JettyConnector() {
		this(new JettyConfiguration());
	}
	
	public JettyConnector(JettyConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public JettyConfiguration getConfiguration() {
		return configuration;
	}
	
	@Override
	public void configure() throws Exception {
		server = new Server(configuration.getPort());
		
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
		context.addServlet(new ServletHolder(new JettyServlet()), "/");
		
		if (configuration.isUseBasicAuth()) { 
			
			SecurityHandler securityHandler = buildSecurityHandler();
			context.setSecurityHandler(securityHandler);
			
		}
		
        server.setHandler(context);

	}
	
	private SecurityHandler buildSecurityHandler() {
		
		SecurityHandler securityHandler = new CustomSecurityHandler(); // use our custom security handler that always forces authentication
		securityHandler.setAuthenticator(new BasicAuthenticator());
		securityHandler.setLoginService(new CustomLoginService());
		
		return securityHandler;
	}

	@Override
	public void doStart() throws Exception {
		server.start();
	}

	@Override
	public void doStop() throws Exception {
		server.stop();
	}
	
	@Override
	public void destroy() throws Exception {
	}
	
	private class JettyServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			
			// GET parameters must be encoded in UTF-8 always
			produceMessage(request, false);
		}

		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			
			
			// POST parameters can be encoded using any charset
			produceMessage(request, true);
		}
		
		@SuppressWarnings("rawtypes")
		private void produceMessage(HttpServletRequest request, boolean decodeParams) throws IOException {
			
			Message message = new Message();
			message.setType(Message.SMS_TYPE);
			
			Enumeration paramNames = request.getParameterNames();
			while (paramNames.hasMoreElements()) {
				String paramName = (String) paramNames.nextElement();
				
				if ("type".equals(paramName)) {
					message.setType(request.getParameter(paramName));
				}
				
				if ("reference".equals(paramName)) {
					message.setReference(request.getParameter(paramName));
				}
				
				// check if there is a mapping for the key
				String key = paramName;
				if (configuration.getMapper().containsKey(key)) {
					key = configuration.getMapper().get(key);
				}
				
				String value = request.getParameter(paramName);
				if (decodeParams) {
					value = decodeParam(value, request.getCharacterEncoding());
				}
				
				message.setProperty(key, value);
			}
	        
	        messageProducer.produce(message);
		}
		
		
		private String decodeParam(String paramValue, String encoding) throws UnsupportedEncodingException {
			
			if (encoding == null) {
				encoding = "UTF-8";
			}
			
			return URLDecoder.decode(paramValue, encoding);
		}
		
	}
	
	private class CustomLoginService extends MappedLoginService {

		@Override
		protected UserIdentity loadUser(String username) {
			
			Map<String,String> users = configuration.getUsers();
			
			if (users.containsKey(username)) {
				
				Credential credential = Credential.getCredential(users.get(username));
				
				Principal userPrincipal = new KnownUser(username, credential);
				
	            Subject subject = new Subject();
	            subject.getPrincipals().add(userPrincipal);
	            subject.getPrivateCredentials().add(credential);
	            subject.setReadOnly();
	            
	            UserIdentity identity= _identityService.newUserIdentity(subject, userPrincipal, IdentityService.NO_ROLES);
			
	            return identity;
				
			}
			
			return null;
		}

		@Override
		protected void loadUsers() throws IOException {
			
		}
		
	}
	
	private class CustomSecurityHandler extends ConstraintSecurityHandler {

		@Override
		protected boolean isAuthMandatory(Request baseRequest, Response base_response, Object constraintInfo) {
			return true;
		}
		
	}

}

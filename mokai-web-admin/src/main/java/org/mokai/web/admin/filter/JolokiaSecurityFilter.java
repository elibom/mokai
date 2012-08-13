package org.mokai.web.admin.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.jasypt.util.password.StrongPasswordEncryptor;
import org.mokai.web.admin.AdminPasswordStore;

/**
 * A security filter for Jolokia JMX support. It uses an {@link AdminPasswordStore} implementation to validate the 
 * password.
 * 
 * @author German Escobar
 */
public class JolokiaSecurityFilter implements Filter {
	
	private AdminPasswordStore adminPasswordStore;
	
	@Override
	public void init(FilterConfig config) throws ServletException {}

	@Override
	public void destroy() {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, 
			ServletException {
		
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		
		String basicAuthHeader = httpRequest.getHeader("Authorization");
		if (basicAuthHeader == null) {
			basicAuthHeader = httpRequest.getHeader("Authentication");
		}
		
		if (basicAuthHeader == null) {
			httpResponse.setStatus(401);
			return;
		}
		
		String[] basicAuth = basicAuthHeader.split(" ");
		if (basicAuth.length == 2) {
			String userpass = basicAuth[1];
			userpass = new String( DatatypeConverter.parseBase64Binary(userpass) );
			
			String[] arrUserpass = userpass.split(":");
			if (arrUserpass.length == 2) {
				String username = arrUserpass[0];
				String password = arrUserpass[1];
				
				if ("admin".equals(username) && isPasswordValid(password)) {
					chain.doFilter(request, response);
					return;
				}
			}
		}
		
		httpResponse.setStatus(401);

	}

	/**
	 * Helper method. Tells if the password is valid or not.
	 * 
	 * @param password the password to validate.
	 * 
	 * @return true if the password is valid, false otherwise.
	 */
	private boolean isPasswordValid(String password) {
		String encryptedPassword = adminPasswordStore.getPassword();
		
		if (encryptedPassword != null) {
			StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
			return encryptor.checkPassword(password, encryptedPassword);
		} else {
			if ("admin".equals(password)) {
				return true;
			}
		}
		
		return false;
		
	}

	public void setAdminPasswordStore(AdminPasswordStore adminPasswordStore) {
		this.adminPasswordStore = adminPasswordStore;
	}

}

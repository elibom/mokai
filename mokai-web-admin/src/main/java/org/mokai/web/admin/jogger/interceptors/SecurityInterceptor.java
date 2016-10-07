package org.mokai.web.admin.jogger.interceptors;

import com.elibom.jogger.http.Cookie;
import com.elibom.jogger.http.Request;
import com.elibom.jogger.http.Response;
import com.elibom.jogger.middleware.router.interceptor.Action;
import com.elibom.jogger.middleware.router.interceptor.Controller;
import com.elibom.jogger.middleware.router.interceptor.Interceptor;
import com.elibom.jogger.middleware.router.interceptor.InterceptorExecution;
import javax.xml.bind.DatatypeConverter;

import org.jasypt.util.password.StrongPasswordEncryptor;
import org.mokai.web.admin.AdminPasswordStore;
import org.mokai.web.admin.jogger.annotations.Secured;

/**
 *
 *
 * @author German Escobar
 */
public class SecurityInterceptor implements Interceptor {

	private AdminPasswordStore adminPasswordStore;

	@Override
	public void intercept(Request request, Response response, InterceptorExecution execution) throws Exception {
		// check if the user is authenticated - cookie or basic
		String basicAuthHeader = request.getHeader("Authentication");
		if (basicAuthHeader == null) {
			basicAuthHeader = request.getHeader("Authorization");
		}
		boolean authenticated = isAuthenticated( request.getCookie("access_token"), basicAuthHeader );

		// this might be used by controllers and views
		response.setAttribute("authenticated", authenticated);

		// check if the action requires authentication
		boolean requiresAuth = requiresAuthentication( execution.getController(), execution.getAction() );

		// if not authenticated redirect (if HTML) or return 401 otherwise
		if (requiresAuth && !authenticated) {
			String acceptHeader = request.getHeader("Accept");
			if (acceptHeader != null && acceptHeader.contains("text/html")) {
				response.redirect("/sessions/new");
				return;
			} else {
				response.unauthorized();
				return;
			}
		}

		execution.proceed();
	}

	/**
	 * Helper method. Tells if the user is authenticated or not. Authentication can be in two forms: as a cookie
	 * (named "access_token") or using the Basic mechanism.
	 *
	 * @param cookie the access_token cookie.
	 * @param basicAuthHeader the basic authentication header.
	 *
	 * @return true if the request is authenticated, false otherwise.
	 */
	private boolean isAuthenticated(Cookie cookie, String basicAuthHeader) {
		if (cookie != null) {
			return true;
		}

		if (basicAuthHeader != null) {
			String[] basicAuth = basicAuthHeader.split(" ");
			if (basicAuth.length == 2) {
				String userpass = basicAuth[1];
				userpass = new String( DatatypeConverter.parseBase64Binary(userpass) );

				String[] arrUserpass = userpass.split(":");
				if (arrUserpass.length == 2) {
					String username = arrUserpass[0];
					String password = arrUserpass[1];

					if ("admin".equals(username) && isPasswordValid(password)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Helper method. Tells if the action requires authentication or not.
	 *
	 * @param controller holds the controller info.
	 * @param action holds the action info.
	 *
	 * @return true if the action requires authentication, false otherwise.
	 */
	private boolean requiresAuthentication(Controller controller, Action action) {
		boolean requiresAuth = controller.getAnnotation(Secured.class) != null;
		if (!requiresAuth) {
			requiresAuth = action.getAnnotation(Secured.class) != null;
		}

		return requiresAuth;
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

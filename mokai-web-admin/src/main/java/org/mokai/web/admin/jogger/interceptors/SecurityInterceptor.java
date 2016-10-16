package org.mokai.web.admin.jogger.interceptors;

import com.elibom.jogger.http.Http;
import com.elibom.jogger.http.Request;
import com.elibom.jogger.http.Response;
import com.elibom.jogger.middleware.router.interceptor.Interceptor;
import com.elibom.jogger.middleware.router.interceptor.InterceptorExecution;
import org.mokai.web.admin.jogger.Annotations.Secured;
import org.mokai.web.admin.jogger.Session;

/**
 *
 *
 * @author German Escobar
 */
public class SecurityInterceptor implements Interceptor {

    @Override
    public void intercept(Request request, Response response, InterceptorExecution execution) throws Exception {

        Session session = (Session) response.getAttributes().get("session");

        boolean requiresAuth = requiresAuthentication(execution);
        if (requiresAuth && session.getUser() == null) {
            if (request.getHeader(Http.Headers.ACCEPT).contains("html")) {
                response.redirect("/login");
            } else {
                response.unauthorized();
            }
            return;
        }
        execution.proceed();
    }

    private boolean requiresAuthentication(InterceptorExecution execution) {
        boolean requiresAuth = execution.getController().getAnnotation(Secured.class) != null;
        if (!requiresAuth) {
            requiresAuth = execution.getAction().getAnnotation(Secured.class) != null;
        }

        return requiresAuth;
    }

}

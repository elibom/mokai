package org.mokai.web.admin.jogger.interceptors;

import com.elibom.jogger.http.Request;
import com.elibom.jogger.http.Response;
import com.elibom.jogger.middleware.router.interceptor.Interceptor;
import com.elibom.jogger.middleware.router.interceptor.InterceptorExecution;

public class NoCacheInterceptor implements Interceptor {

    @Override
    public void intercept(Request request, Response response, InterceptorExecution execution) throws Exception {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        response.setHeader("Expires", "0"); // Proxies.
        execution.proceed();
    }
}

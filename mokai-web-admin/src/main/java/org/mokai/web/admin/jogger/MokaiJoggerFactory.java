package org.mokai.web.admin.jogger;

import com.elibom.jogger.Jogger;
import com.elibom.jogger.Middleware;
import com.elibom.jogger.MiddlewaresFactory;
import com.elibom.jogger.middleware.router.RouterMiddleware;
import com.elibom.jogger.middleware.router.interceptor.Interceptor;
import com.elibom.jogger.middleware.router.loader.RoutesLoader;
import com.elibom.jogger.middleware.statik.StaticMiddleware;
import com.elibom.jogger.template.FreemarkerTemplateEngine;
import freemarker.template.Configuration;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 *
 * @author Alejandro Riveros <lariverosc at gmail.com>
 */
public class MokaiJoggerFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private RoutesLoader routesLoader;

    private Configuration freeMarker;

    private String staticDirectory = "static";

    public Jogger create() throws Exception {
        Jogger app = new Jogger(new MiddlewaresFactory() {

            @Override
            public Middleware[] create() throws Exception {
                RouterMiddleware router = new RouterMiddleware();
                router.setRoutes(routesLoader.load());

                // add interceptors
                Interceptor freemarkerInterceptor = applicationContext.getBean("freemarkerInterceptor", Interceptor.class);
                router.addInterceptor(freemarkerInterceptor);
                Interceptor sessionInterceptor = applicationContext.getBean("sessionInterceptor", Interceptor.class);
                router.addInterceptor(sessionInterceptor);
                Interceptor securityInterceptor = applicationContext.getBean("securityInterceptor", Interceptor.class);
                router.addInterceptor(securityInterceptor);
                Interceptor loggingInterceptor = applicationContext.getBean("loggingInterceptor", Interceptor.class);
                router.addInterceptor(loggingInterceptor);
                Interceptor notAuhtenticatedInterceptor = applicationContext.getBean("notAuthenticatedInterceptor", Interceptor.class);
                router.addInterceptor(notAuhtenticatedInterceptor);

                return new Middleware[]{new StaticMiddleware(staticDirectory, "static"), router};
            }

        });
        app.setTemplateEngine(new FreemarkerTemplateEngine(freeMarker));

        return app;
    }

    public void setRoutesLoader(RoutesLoader routesLoader) {
        this.routesLoader = routesLoader;
    }

    public void setFreeMarker(Configuration freeMarker) {
        this.freeMarker = freeMarker;
    }

    public void setStaticDirectory(String staticDirectory) {
        this.staticDirectory = staticDirectory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}

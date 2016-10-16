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
 * @author Alejandro <lariverosc@gmail.com>
 */
public class MokaiJoggerFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private RoutesLoader routesLoader;

    private String staticDirectory = "static";

    private Configuration freeMarker;

    public Jogger create() throws Exception {

        Jogger joggerApp = new Jogger(new MiddlewaresFactory() {

            @Override
            public Middleware[] create() throws Exception {
                RouterMiddleware routerMiddleware = new RouterMiddleware();
                routerMiddleware.setRoutes(routesLoader.load());
                Interceptor sessionInterceptor = applicationContext.getBean("sessionInterceptor", Interceptor.class);
                routerMiddleware.addInterceptor(sessionInterceptor);
                Interceptor securityInterceptor = applicationContext.getBean("securityInterceptor", Interceptor.class);
                routerMiddleware.addInterceptor(securityInterceptor);
                Interceptor noCaheInterceptor = applicationContext.getBean("noCacheInterceptor", Interceptor.class);
                routerMiddleware.addInterceptor(noCaheInterceptor);

                return new Middleware[]{new StaticMiddleware(staticDirectory, ""), routerMiddleware};
            }
        });
        joggerApp.setTemplateEngine(new FreemarkerTemplateEngine(freeMarker));

        return joggerApp;
    }

    public void setRoutesLoader(RoutesLoader routesLoader) {
        this.routesLoader = routesLoader;
    }

    public void setStaticDirectory(String staticDirectory) {
        this.staticDirectory = staticDirectory;
    }

    public void setFreeMarker(Configuration freeMarker) {
        this.freeMarker = freeMarker;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}

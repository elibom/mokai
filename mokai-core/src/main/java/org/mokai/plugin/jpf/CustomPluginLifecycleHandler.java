package org.mokai.plugin.jpf;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;

import org.java.plugin.PluginClassLoader;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.standard.StandardPluginLifecycleHandler;

/**
 * A custom {@link PluginLifecycleHandler} that ignores the services files from the
 * parent class loader. This way it will ONLY find the service files of the plugins. 
 * 
 * @author German Escobar
 */
public class CustomPluginLifecycleHandler extends StandardPluginLifecycleHandler {

	@Override
	protected PluginClassLoader createPluginClassLoader(final PluginDescriptor descr) {
		
		final ClassLoader parentClassLoader = new ClassLoader() {

			@Override
			public Enumeration<URL> getResources(String name) throws IOException {
				
				return new Enumeration<URL>() {

					@Override
					public boolean hasMoreElements() {
						return false;
					}

					@Override
					public URL nextElement() {
						return null;
					}
					
				};
			}
			
		};
		
		CustomPluginClassLoader result = AccessController
				.doPrivileged(new PrivilegedAction<CustomPluginClassLoader>() {
					public CustomPluginClassLoader run() {
						return new CustomPluginClassLoader(
								getPluginManager(), descr, parentClassLoader);
					}
				});
		result.setProbeParentLoaderLast(true);

		return result;
	}

	
}

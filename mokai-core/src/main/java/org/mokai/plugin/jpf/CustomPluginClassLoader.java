package org.mokai.plugin.jpf;

import org.java.plugin.PluginManager;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.standard.StandardPluginClassLoader;

/**
 *
 * @author German Escobar
 */
public class CustomPluginClassLoader extends StandardPluginClassLoader {

	public CustomPluginClassLoader(PluginManager aManager,
			PluginDescriptor descr, ClassLoader parent) {
		super(aManager, descr, parent);
	}

	@Override
	public final void setProbeParentLoaderLast(boolean value) {
		super.setProbeParentLoaderLast(value);
	}

}

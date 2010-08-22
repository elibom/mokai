package org.mokai.plugin;

/**
 * Wraps any exception in the {@link PluginMechanism} implementations.
 * 
 * @author German Escobar
 */
public class PluginException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PluginException() {
	}

	public PluginException(String message) {
		super(message);
	}

	public PluginException(Throwable cause) {
		super(cause);
	}

	public PluginException(String message, Throwable cause) {
		super(message, cause);
	}

}

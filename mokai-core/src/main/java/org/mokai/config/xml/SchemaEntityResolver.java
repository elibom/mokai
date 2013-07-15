package org.mokai.config.xml;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An {@link EntityResolver} implementation that resolves URL's schema locations
 * to classpath lookups. Any systemId URL beginning with http://mokaigateway.org/ is
 * searched in the com/mokai/config/xml classpath.
 *
 * @author German Escobar
 */
public class SchemaEntityResolver implements EntityResolver {

	private Logger log = LoggerFactory.getLogger(SchemaEntityResolver.class);

	private static final String MOKAI_NAMESPACE = "http://mokai.googlecode.com/svn/schema/";

	@Override
	public final InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {

		if (systemId != null) {
			log.debug("trying to resolve system-id [" + systemId + "]");

			if (systemId.startsWith(MOKAI_NAMESPACE)) {

				String path = "org/mokai/config/xml/" + systemId.substring(MOKAI_NAMESPACE.length());

				InputStream dtdStream = resolveInMokaiNamespace(path);
				if (dtdStream == null)  {
					log.warn("unable to locate [" + systemId + "] on classpath");
				} else {
					log.debug("located [" + systemId + "] in classpath");
					InputSource source = new InputSource(dtdStream);
					source.setPublicId(publicId);
					source.setSystemId(systemId);
					return source;
				}
			}
		}

		return null;
	}

	protected final InputStream resolveInMokaiNamespace(String path) {
		return this.getClass().getClassLoader().getResourceAsStream(path);
	}
}

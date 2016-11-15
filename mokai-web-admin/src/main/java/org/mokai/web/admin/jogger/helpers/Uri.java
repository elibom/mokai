package org.mokai.web.admin.jogger.helpers;

import java.net.URI;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro <lariverosc@gmail.com>
 */
public class Uri {

    private final Logger log = LoggerFactory.getLogger(Uri.class);

    private final URI uri;

    /**
     * Constructor. Receives the uri that we are going to parse.
     *
     * @param uri
     */
    public Uri(String uri) {
        try {
            this.uri = new URI(uri);
        } catch (URISyntaxException ex) {
            log.error("Error while parsing URL", ex);
            throw new RuntimeException(ex);
        }
    }

    public String getUrl() {
        return uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort();
    }

    public String getHost() {
        return uri.getHost();
    }

    public String getScheme() {
        return uri.getScheme();
    }

    public int getPort() {
        return uri.getPort();
    }

    public String getPath() {
        String path = uri.getPath();
        if (uri.getQuery() != null) {
            path += "?" + uri.getQuery();
        }
        return path;
    }

    public String getUsername() {
        if (uri.getUserInfo() != null) {
            return uri.getUserInfo().split(":", 2)[0];
        }
        return null;
    }

    public String getPassword() {
        if (uri.getUserInfo() != null) {
            return uri.getUserInfo().split(":", 2)[1];
        }
        return null;
    }
}

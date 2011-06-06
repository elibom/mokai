package org.mokai.connector.http;

import java.util.HashMap;
import java.util.Map;

import org.mokai.ui.annotation.Label;
import org.mokai.ui.annotation.Required;

public class HttpConfiguration {

	@Required
	@Label("Url")
	private String url;
	
	@Label("Throw Exception on Failure")
	private boolean throwExceptionOnFailure = true;
	
	@Label("Auth - Method")
	private String authMethod;
	
	@Label("Auth - Username")
	private String authUsername;
	
	@Label("Auth - Password")
	private String authPassword;
	
	@Label("Additional Query")
	private String additionalQuery;
	
	@Label("Mapper")
	private Map<String,String> mapper = new HashMap<String,String>();

	public final String getUrl() {
		return url;
	}

	public final void setUrl(String url) {
		this.url = url;
	}

	public final boolean isThrowExceptionOnFailure() {
		return throwExceptionOnFailure;
	}

	public final void setThrowExceptionOnFailure(boolean throwExceptionOnFailure) {
		this.throwExceptionOnFailure = throwExceptionOnFailure;
	}

	public final String getAuthMethod() {
		return authMethod;
	}

	public final void setAuthMethod(String authMethod) {
		this.authMethod = authMethod;
	}

	public final String getAuthUsername() {
		return authUsername;
	}

	public final void setAuthUsername(String authUsername) {
		this.authUsername = authUsername;
	}

	public final String getAuthPassword() {
		return authPassword;
	}

	public final void setAuthPassword(String authPassword) {
		this.authPassword = authPassword;
	}

	public final String getAdditionalQuery() {
		return additionalQuery;
	}

	public final void setAdditionalQuery(String additionalQuery) {
		this.additionalQuery = additionalQuery;
	}

	public final Map<String, String> getMapper() {
		return mapper;
	}

	public final void setMapper(Map<String, String> mapper) {
		this.mapper = mapper;
	}
	
	public final void addMapper(String key, String value) {
		mapper.put(key, value);
	}
	
}

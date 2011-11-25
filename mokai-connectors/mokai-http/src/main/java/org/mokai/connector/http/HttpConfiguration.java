package org.mokai.connector.http;

import java.util.HashMap;
import java.util.Map;

import org.mokai.ui.annotation.Label;
import org.mokai.ui.annotation.Required;

public class HttpConfiguration {

	@Required
	@Label("Url")
	private String url;
	
	@Label("Http Method")
	private String method = "GET";
	
	@Label("Encoding")
	private String encoding = "ISO-8859-1";
	
	@Label("Throw Exception on Failure")
	private boolean throwExceptionOnFailure = true;
	
	@Label("Additional Params")
	private Map<String,String> additionalParams = new HashMap<String,String>();
	
	@Label("Basic Auth")
	private boolean basicAuth;
	
	@Label("Basic - Username")
	private String username;
	
	@Label("Basic - Password")
	private String password;
	
	@Label("Mapper")
	private Map<String,String> mapper = new HashMap<String,String>();
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public boolean isThrowExceptionOnFailure() {
		return throwExceptionOnFailure;
	}

	public void setThrowExceptionOnFailure(boolean throwExceptionOnFailure) {
		this.throwExceptionOnFailure = throwExceptionOnFailure;
	}

	public Map<String, String> getAdditionalParams() {
		return additionalParams;
	}

	public void setAdditionalParams(Map<String, String> additionalParams) {
		this.additionalParams = additionalParams;
	}

	public boolean isBasicAuth() {
		return basicAuth;
	}

	public void setBasicAuth(boolean basicAuth) {
		this.basicAuth = basicAuth;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Map<String, String> getMapper() {
		return mapper;
	}

	public void setMapper(Map<String, String> mapper) {
		this.mapper = mapper;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
}

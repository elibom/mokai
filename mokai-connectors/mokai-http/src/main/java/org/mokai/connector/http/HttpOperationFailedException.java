package org.mokai.connector.http;

import org.mokai.ExecutionException;

public class HttpOperationFailedException extends ExecutionException {

	private static final long serialVersionUID = 1L;

	private String url;
	
	private String redirectLocation;
	
	private int statusCode;
	
	private String statusText;
	
	private String responseBody;
	
	public HttpOperationFailedException(String url, String redirectLocation, int statusCode, 
				String statusText, String responseBody) {
		
		super("HTTP operation failed invoking " + url + " with statusCode: " 
				+ statusCode 
				+ (redirectLocation != null ? ", redirectLocation: " + redirectLocation : ""));
		
		this.url = url;
		this.redirectLocation = redirectLocation;
		this.statusCode = statusCode;
		this.statusText = statusText;
		this.responseBody = responseBody;
	}

	public final String getUrl() {
		return url;
	}

	public final void setUrl(String url) {
		this.url = url;
	}

	public final String getRedirectLocation() {
		return redirectLocation;
	}

	public final void setRedirectLocation(String redirectLocation) {
		this.redirectLocation = redirectLocation;
	}

	public final int getStatusCode() {
		return statusCode;
	}

	public final void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public final String getStatusText() {
		return statusText;
	}

	public final void setStatusText(String statusText) {
		this.statusText = statusText;
	}

	public final String getResponseBody() {
		return responseBody;
	}

	public final void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}
	
}

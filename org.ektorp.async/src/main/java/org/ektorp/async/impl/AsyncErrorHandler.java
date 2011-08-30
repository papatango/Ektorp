package org.ektorp.async.impl;

import java.io.UnsupportedEncodingException;

import org.ektorp.http.ErrorStatusHandler;
import org.ektorp.util.Exceptions;

import com.ning.http.client.HttpResponseStatus;

public class AsyncErrorHandler<T> extends BufferingResponseHandler<T> {

	private final HttpResponseStatus responseStatus;
	
	public AsyncErrorHandler(HttpResponseStatus responseStatus) {
		this.responseStatus = responseStatus;
	}
	
	public T onCompleted() {
		try {
			String responseBody = new String(responseBodyBuffer.toByteArray(), "UTF-8");
			throw ErrorStatusHandler.createDbAccessException(responseStatus.getStatusCode(), responseStatus.getStatusText(), responseBody, responseStatus.getUrl().toString());	
		} catch(UnsupportedEncodingException e) {
			throw Exceptions.propagate(e);
		}
		
	}
	
}

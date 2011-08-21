package org.ektorp.async.impl;

import java.io.IOException;

import org.ektorp.DbAccessException;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.UpdateConflictException;
import org.ektorp.http.HttpStatus;
import org.xml.sax.ErrorHandler;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
/**
 * 
 * @author henrik
 *
 * @param <T>
 */
public class StdResponseHandler<T> implements AsyncHandler<T> {

	private ResponseHandler responseHandler;
	
	@Override
	public void onThrowable(Throwable t) {
		
	}

	@Override
	public com.ning.http.client.AsyncHandler.STATE onBodyPartReceived(
			HttpResponseBodyPart bodyPart) throws Exception {
		bodyPart.writeTo(responseHandler.getBodyStream());
		return STATE.CONTINUE;
	}

	@Override
	public com.ning.http.client.AsyncHandler.STATE onStatusReceived(
			HttpResponseStatus responseStatus) throws Exception {
		if (responseStatus.getStatusCode() < 300) {
			responseHandler = new AsyncErrorhandler(responseStatus);
		}
		return null;
	}

	protected STATE onError(HttpResponseStatus responseStatus) {
		return null;
	}
	
	@Override
	public com.ning.http.client.AsyncHandler.STATE onHeadersReceived(
			HttpResponseHeaders headers) throws Exception {
		return null;
	}

	@Override
	public T onCompleted() throws Exception {
		responseHandler.onCompleted();
		return null;
	}

}

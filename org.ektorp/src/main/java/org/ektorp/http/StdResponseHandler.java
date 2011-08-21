package org.ektorp.http;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.ektorp.DbAccessException;
import org.ektorp.util.Exceptions;

/**
 * 
 * @author henrik lundgren
 *
 * @param <T>
 */
public class StdResponseHandler<T> implements ResponseCallback<T> {	
	/**
	 * Creates an DbAccessException which specific type is determined by the response code in the http response.
	 * @param hr
	 * @return
	 */
	public static DbAccessException createDbAccessException(HttpResponse hr) {
		try {
			return ErrorStatusHandler.createDbAccessException(hr.getCode(), "", IOUtils.toString(hr.getContent()), hr.getRequestURI());
		} catch (IOException e) {
			throw Exceptions.propagate(e);
		}
	}
	

	public T error(HttpResponse hr) {
		throw StdResponseHandler.createDbAccessException(hr);
	}
	
	public T success(HttpResponse hr) throws Exception {
		return null;
	}

}

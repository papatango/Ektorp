package org.ektorp.async.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
/**
 * 
 * @author henrik
 *
 * @param <T>
 */
public abstract class BufferingResponseHandler<T> implements
		AsyncResponseHandler<T> {

	protected final ByteArrayOutputStream responseBodyBuffer;
	/**
	 * 
	 */
	public BufferingResponseHandler() {
		responseBodyBuffer = new ByteArrayOutputStream();
	}
	/**
	 * 
	 * @param size the initial size
	 * 
	 */
	public BufferingResponseHandler(int size) {
		responseBodyBuffer = new ByteArrayOutputStream(size);
	}

	@Override
	public OutputStream getBodyStream() {
		return responseBodyBuffer;
	}

	protected InputStream getBody() {
		return new ByteArrayInputStream(responseBodyBuffer.toByteArray());
	}
}

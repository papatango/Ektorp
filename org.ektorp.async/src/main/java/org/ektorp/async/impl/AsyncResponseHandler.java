package org.ektorp.async.impl;

import java.io.OutputStream;

public interface AsyncResponseHandler<T> {

	OutputStream getBodyStream();
	T onCompleted() throws Exception;
	
}

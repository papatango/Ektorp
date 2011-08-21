package org.ektorp.async;

public interface CompletionHandler<T> {

	T success() throws Exception;
	
}

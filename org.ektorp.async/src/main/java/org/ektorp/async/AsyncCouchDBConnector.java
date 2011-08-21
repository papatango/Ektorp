package org.ektorp.async;

import java.util.concurrent.Future;

import com.ning.http.client.AsyncHandler;

public interface AsyncCouchDBConnector {

	<T> Future<T> get(Class<T> c, String id);
	<T> Future<T> get(String id, AsyncHandler<T> handler);
	
}

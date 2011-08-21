package org.ektorp.async.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;

import org.ektorp.DbPath;
import org.ektorp.async.AsyncCouchDBConnector;
import org.ektorp.http.URI;
import org.ektorp.util.Exceptions;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;

public class StdAsyncCouchDBConnector implements AsyncCouchDBConnector {

	private final AsyncHttpClient httpClient;
	private final String dbName;
	private URI dbURI;
	
	public StdAsyncCouchDBConnector(String databaseName) {
		 this(databaseName, new AsyncHttpClient());
	}
	
	public StdAsyncCouchDBConnector(String dbUrl, AsyncHttpClient client) {
		URL u;
		try {
			u = new URL(dbUrl);
		} catch (MalformedURLException e) {
			throw Exceptions.propagate(e);
		}
		dbName = u.getPath();
		this.dbURI = URI.prototype(u.toString());
		this.httpClient = client;
	}
	
	@Override
	public <T> Future<T> get(Class<T> c, String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Future<T> get(String id, final AsyncHandler<T> handler) {
		BoundRequestBuilder rb = httpClient.prepareGet(dbURI.append(id).toString());
		try {
			return rb.execute(handler);
		} catch (IOException e) {
			throw Exceptions.propagate(e);
		}
	}

}

package org.ektorp.async.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;

public class StdAsyncCouchDBConnectorTest {

	@Test
	public void testGet() throws InterruptedException, ExecutionException, IOException {
		AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
											.setAllowPoolingConnection(true)
											.build();
		
		AsyncHttpClient client = new AsyncHttpClient(config);
		
		StdAsyncCouchDBConnector db = new StdAsyncCouchDBConnector("http://localhost:5984/blogposts", client);
		final ObjectMapper mapper = new ObjectMapper();
		final PipedInputStream in = new PipedInputStream();
		final PipedOutputStream out = new PipedOutputStream(in);
		
		Future<String> f = db.get("_design/BlogPost/_view/all", new AsyncHandler<String>() {

			@Override
			public void onThrowable(Throwable t) {
				t.printStackTrace();
			}

			@Override
			public com.ning.http.client.AsyncHandler.STATE onBodyPartReceived(
					HttpResponseBodyPart bodyPart) throws Exception {
				bodyPart.writeTo(out);
				return STATE.CONTINUE;
			}

			@Override
			public com.ning.http.client.AsyncHandler.STATE onStatusReceived(
					HttpResponseStatus responseStatus) throws Exception {
				return STATE.CONTINUE;
			}

			@Override
			public com.ning.http.client.AsyncHandler.STATE onHeadersReceived(
					HttpResponseHeaders headers) throws Exception {
				System.out.println(headers.toString());
				return STATE.CONTINUE;
			}

			@Override
			public String onCompleted() throws Exception {
				System.out.println("completed");
				return "qwerty";
			}});
		
		JsonNode node = mapper.readTree(in);
		System.out.println(node);
		f.get();
	}

}

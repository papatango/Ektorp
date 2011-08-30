package org.ektorp.http;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.NullNode;
import org.ektorp.DbAccessException;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.UpdateConflictException;

public class ErrorStatusHandler {

	private final static ObjectMapper MAPPER = new ObjectMapper();
	
	/**
	 * 
	 * @param statusCode
	 * @param reasonPhrase
	 * @param body
	 * @param url
	 * @return
	 */
	public static DbAccessException createDbAccessException(int statusCode, String reasonPhrase, String body, String url) {
		JsonNode responseBody;
		try {
			responseBody = responseBodyAsNode(body);
		} catch (IOException e) {
			responseBody = NullNode.getInstance();
		}
		switch (statusCode) {
		case HttpStatus.NOT_FOUND:
			return new DocumentNotFoundException(url, responseBody);
		case HttpStatus.CONFLICT:
			return new UpdateConflictException();
		default:
			String message;
			try {
				message = toPrettyString(responseBody);
			} catch (IOException e) {
				message = "unavailable";
			}
			return new DbAccessException(statusCode + ":" + reasonPhrase + "\nURI: " + url + "\nResponse Body: \n" + message);
		}
	}
	
	private static String toPrettyString(JsonNode n) throws IOException {
		return MAPPER.defaultPrettyPrintingWriter().writeValueAsString(n);
	}
	
	private static JsonNode responseBodyAsNode(String s) throws IOException {
		if (s == null || s.length() == 0) {
			return NullNode.getInstance();
		} else if (!s.startsWith("{")) {
			return NullNode.getInstance();
		}
		return MAPPER.readTree(s);
	}
	
}

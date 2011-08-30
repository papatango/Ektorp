package org.ektorp.async.impl;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * 
 * @author henrik lundgren
 *
 * @param <T>
 */
public class ObjectMappingResponseHandler<T> extends BufferingResponseHandler<T> {

	private final ObjectMapper mapper;
	private final Class<T> type;
	
	public ObjectMappingResponseHandler(Class<T> type, ObjectMapper om) {
		this.mapper = om;
		this.type = type;
	}
	
	@Override
	public T onCompleted() throws Exception {
		return mapper.readValue(getBody(), type);
	}

}

package org.ektorp.http;

import java.io.*;
import java.net.*;
import java.util.Map;

import org.ektorp.Spatial_type;
import org.ektorp.util.*;
/**
 * Modified to overload the params method so we can do geometry=POLYGON or 
 * some other type in future.
 * @author Paul Torres (modifications only)
 *
 */
public class URI {

	private final StringBuilder path;
	private final boolean prototype;
	private StringBuilder params;
	private String uri;
	
	private URI(String path) {
		this.path = new StringBuilder(path);
		prototype = false;
	}
	
	private URI(String path, boolean prototype) {
		this.path = new StringBuilder(path);
		this.prototype = prototype;
	}
	
	private URI(StringBuilder path, StringBuilder params) {
		this.path = path;
		this.params = params;
		prototype = false;
	}
	
	private URI(StringBuilder path) {
		this(path, null);
	}
	
	public static URI of(String path) {
		return new URI(path);
	}
	
	public static URI prototype(String path) {
		return new URI(path, true);
	}

	public URI copy() {
		return params != null ? new URI(new StringBuilder(path), 
		    new StringBuilder(params)) : new URI(new StringBuilder(path));
	}
	
	public URI append(String pathElement) {
		if (prototype) {
			return copy().append(pathElement);
		}
		if (path.charAt(path.length()-1) != '/') {
			path.append("/");	
		}
		try {
			if (!pathElement.startsWith("_")) {
				pathElement = URLEncoder.encode(pathElement, "UTF-8"); 
			}
			path.append(pathElement);
		} catch (UnsupportedEncodingException e) {
			throw Exceptions.propagate(e);
		}
		uri = null;
		return this;
	}
	/**
	 * The original method
	 * @param name
	 * @param value
	 * @return
	 */
	 public URI param(String name, String value) {
	    if (prototype) {
	      return copy().param(name, value);
	    }
	    if (params != null) {
	      params().append("&");
	    } else {
	      params().append("?");
	    }
	    try {
	      params().append(name).append("=").append(
	          URLEncoder.encode(value, "UTF-8"));
	    } catch (UnsupportedEncodingException e) {
	      throw Exceptions.propagate(e);
	    }
	    uri = null;
	    return this;
	  }

	/**
	 * Overloaded and modified to support geocouch spatial args which need 
	 * special handling. For now only the geometry=POLYGON and bbox searches are
	 * supported, but more changes could support other types, although polygon
	 * searches really are the most popular outside of bounding boxes.
	 * @param name
	 * @param value
	 * @return
	 * Paul Torres
	 */
	public URI param(String name, String value, Spatial_type t) {
		if (prototype) {
			return copy().param(name, value);
		}
		//params must be null...nonsensical otherwise to specify 2 polygons or 
		//bboxes although this would never happen anyway...
		if(params != null) {
		  throw new IllegalStateException("Cannot specify more than 1 set of " +
		  		"coordinates for spatial searches.");
		} else {
			params().append("?");
		}
		
		StringBuilder sb = new StringBuilder(value);
    if(name.contentEquals("geometry")){
      sb.insert(0, (t.get_type_as_String() + "(("));
      sb.append("))");
      try {
        params().append(name).append("=").append(
            URLEncoder.encode(sb.toString(), "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        throw Exceptions.propagate(e);
      }
    } else if(name.contentEquals("bbox")) {		
      try {
        params().append(name).append("=").append(
            URLEncoder.encode(value, "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        throw Exceptions.propagate(e);
      }
    }
    uri = null;
    return this;
	}

	public URI param(String name, int value) {
		return param(name, Integer.toString(value));
	}
	
	public URI param(String name, long value) {
		return param(name, Long.toString(value));
	}
	
	private StringBuilder params() {
		if (params == null) {
			params = new StringBuilder();
		}
		return params;
	}
	
	@Override
	public String toString() {
		if (uri == null) {
			uri = params != null ? path.append(params).toString() : 
			  path.toString(); 
		}
		return uri;
	}

	public void params(Map<String, String> params) { 
		for (Map.Entry<String, String> e : params.entrySet()) {
			param(e.getKey(), e.getValue());
		}
	}
}
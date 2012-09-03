package org.ektorp;

import java.io.*;
import java.util.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.annotate.*;
import org.ektorp.util.*;

/**
 * @author henrik lundgren
 * <p>
 * Added support for 2 special kinds of responses: totally reduced views of the
 * form: {"rows":[{"key":null,"value":0}]}, and reduced spatial views of the 
 * form: {"count":0}. Subclassing wouldn't work as far as I could see, nor would
 * the creation of 2 additional classes, even though I thought the latter 
 * desireable. 
 * Paul Torres
 */
public class ViewResult implements Iterable<ViewResult.Row>, Serializable {

	private static final String OFFSET_FIELD_NAME = "offset";
	private static final String TOTAL_ROWS_FIELD_NAME = "total_rows";
	private static final String UPDATE_SEQ = "update_seq";
	private static final long serialVersionUID = 4750290767933801714L;
	private int totalRows = -1;
	private int offset = -1;
	private String updateSeq;
	private List<Row> rows;
    private final boolean ignoreNotFound;
	
	public ViewResult(JsonNode resultNode, boolean ignoreNotFound) {
	  //System.out.println("json resultnode:" + resultNode.toString());
		this.ignoreNotFound = ignoreNotFound;
        Assert.notNull(resultNode, "resultNode may not be null");
        //if node has no rows but only a count field (from a geocouch spatial 
        //search using count=true query parameter) then construct a single
        //row response with only a count field within it...bit of a hack
        //and better not try to access the other non-existent properties
        if(resultNode.has("count")){
          //System.out.print("resultNode has 'count' within it.");
        	rows = new ArrayList<ViewResult.Row>(1);
        	rows.add(new Row(resultNode));
        	return;
        }       
		Assert.isTrue(resultNode.findPath("rows").isArray(), "result must contain 'rows' field of array type");
		if (resultNode.get(TOTAL_ROWS_FIELD_NAME) != null) {
			totalRows = resultNode.get(TOTAL_ROWS_FIELD_NAME).getIntValue();
		}
		if (resultNode.get(OFFSET_FIELD_NAME) != null) {
			offset = resultNode.get(OFFSET_FIELD_NAME).getIntValue();
		}
		if (resultNode.get(UPDATE_SEQ) != null) {
			updateSeq = resultNode.get(UPDATE_SEQ).getTextValue();
                        if(updateSeq == null) {
                                updateSeq = Long.toString(resultNode.get(UPDATE_SEQ).getIntValue());
                        }
		}
		JsonNode rowsNode = resultNode.get("rows");
		rows = new ArrayList<ViewResult.Row>(rowsNode.size());
		for (JsonNode n : rowsNode) {
		    if (!(ignoreNotFound && n.has(Row.ERROR_FIELD_NAME))) {
		        rows.add(new Row(n)); 		        
		    } 
		}
	}
	
	/**
	 * Get the value in the only row--presumes this response was of the form of
	 * a totally reduced view like so: {"rows":[{"key":null,"value":0}]} or 
	 * {"count":0} from a geospatial search (using geocouch extension), 
	 * otherwise it is non-sensical. Client code must know what it is doing or 
	 * else you'll get IllegalStateException. 
	 * @return 
	 */
	public int getCount(){
	  if(rows.size()>1)
	    throw new IllegalStateException();
	  else
	    return rows.get(0).getCountasInt();   
	}
	
	public List<Row> getRows() {
		return rows;
	}
	
	public int getSize() {
		return rows.size();
	}
	/**
	 * 
	 * @return -1 if result did not contain an offset field
	 */
	public int getOffset() {
		return offset;
	}
	
	@JsonProperty
	void setOffset(int offset) {
		this.offset = offset;
	}
	/**
	 * 
	 * @return -1 if result did not contain a total_rows field
	 */
	public int getTotalRows() {
		return totalRows;
	}
	
	@JsonProperty(TOTAL_ROWS_FIELD_NAME)
	void setTotalRows(int i) {
		this.totalRows = i;
	}

	/**
	 * @return -1L if result did not contain an update_seq field
	 */
	public long getUpdateSeq() {
		if(updateSeq != null) {
			return Long.parseLong(updateSeq);
		}
		return -1L;
	}

	/**
	 * @return false if db is an Cloudant instance.
	 */
	public boolean isUpdateSeqNumeric() {
		return updateSeq != null && updateSeq.matches("^\\d*$");
	}

	/**
	 *
	 * @return null if result did not contain an update_seq field
	 */
	public String getUpdateSeqAsString() {
		return updateSeq;
	}

	@JsonProperty(UPDATE_SEQ)
	public void setUpdateSeq(String updateSeq) {
		this.updateSeq = updateSeq;
	}

	public Iterator<ViewResult.Row> iterator() {
		return rows.iterator();
	}
	
	public boolean isEmpty() {
		return rows.isEmpty();
	}
	
	public static class Row {
		
		static final String VALUE_FIELD_NAME = "value";
		static final String ID_FIELD_NAME = "id";
		static final String KEY_FIELD_NAME = "key";
		static final String DOC_FIELD_NAME = "doc";
		static final String ERROR_FIELD_NAME = "error";
		//geocouch support
		static final String COUNT = "count";
		private final JsonNode rowNode;
		
		@JsonCreator
		public Row(JsonNode rowNode) {
			Assert.notNull(rowNode, "row node may not be null");
			this.rowNode = rowNode;
			if (getError() != null) {
				throw new ViewResultException(getKeyAsNode(), getError());
			}
		}
		//geocouch support--specifically for count=true results of the form 
		//{"count":0}
		public String getCount(){
			return rowNode.get(COUNT).getValueAsText();
		}
        //geocouch support--specifically for count=true results of the form 
		//{"count":0}
        public int getCountasInt(){
            return rowNode.get(COUNT).getIntValue();
        }
		
		public String getId() {
			return rowNode.get(ID_FIELD_NAME).getTextValue();
		}
		
		public String getKey() {
			return nodeAsString(getKeyAsNode());
		}
		
		public JsonNode getKeyAsNode() {
			return rowNode.findPath(KEY_FIELD_NAME);
		}
		
		public String getValue() {
			return nodeAsString(getValueAsNode());
		}
		
		public int getValueAsInt() {
			return getValueAsNode().getValueAsInt(0);
		}
		
		public JsonNode getValueAsNode() {
			return rowNode.findPath(VALUE_FIELD_NAME);
		}
		
		public String getDoc() {
			return nodeAsString(rowNode.findValue(DOC_FIELD_NAME));
		}
		
		public JsonNode getDocAsNode() {
			return rowNode.findPath(DOC_FIELD_NAME);
		}
		
		private String getError() {
			return nodeAsString(rowNode.findValue(ERROR_FIELD_NAME));
		}
		
		private String nodeAsString(JsonNode node) {
			if (isNull(node)) return null;
			return node.isContainerNode() ? node.toString() : node.getValueAsText();
		}

		private boolean isNull(JsonNode node) {
			return node == null || node.isNull() || node.isMissingNode();
		}

	}
	
}

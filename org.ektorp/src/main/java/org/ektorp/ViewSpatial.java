package org.ektorp;

import org.codehaus.jackson.map.ObjectMapper;
import org.ektorp.http.URI;
import org.ektorp.impl.StdObjectMapperFactory;
import org.ektorp.util.Assert;
/**
 * Much better than first attempt to support geocouch (which was just hacking 
 * the viewquery class in situ)...though this class owes everything to the
 * ViewQuery class for its impetus and overall design.
 * @author Paul Torres
 *
 */
public class ViewSpatial {

	private final static ObjectMapper DEFAULT_MAPPER = 
			new StdObjectMapperFactory().createObjectMapper();
	private final static int NOT_SET = -1;
	private ObjectMapper mapper;
	//perhaps in future
//	private double lat_lowerleft = 0.0;
//	private double long_lowerleft = 0.0;
//	private double lat_upperright = 0.0;
//	private double long_upperright = 0.0;
	private static String boundingboxparameter = "bbox";
	private String dbPath;
	private String designDocId;
	private static String spatialfunction = "_spatial";
	//save us the trouble of making this variable...please always name your 
	//spatial function as per example provided in the geocouch example
	private static String spatialfunctioname = "points";
	private int limit = NOT_SET;
	private String staleOk;
	private int skip = NOT_SET;
	private boolean count = false;
	private boolean ignoreNotFound = true;
	private String bboxpoints = "";//"0,0,0,0";
	//default plane-bounds for use with gps coordinates
	private String planebounds = null; //"-180,-90,180,90";	
	private String cachedQuery;
	private String listName;

	public ViewSpatial() {
		mapper = DEFAULT_MAPPER;
	}
	/**
	 * Bring your own ObjectMapper.
	 * The mapper is used when serializing keys when building the query.
	 * @param om
	 */
	public ViewSpatial(ObjectMapper om) {
		Assert.notNull(om, "ObjectMapper may not be null");
		mapper = om;
	}
	
    public String getDbPath() {
        return dbPath;
    }

    public String getDesignDocId() {
        return designDocId;
    }

    public int getLimit() {
        return limit;
    }

    public boolean isStaleOk() {
        return staleOk != null && "ok".equals(staleOk);
    }

    public int getSkip() {
        return skip;
    }
    
	boolean isCount() {
		return count;
	}
	
	public ViewSpatial setCount(boolean count) {
		reset();
		this.count = count;
		return this;
	}
	
    public ViewSpatial dbPath(String s) {
		reset();
		dbPath = s;
		return this;
	}

	public ViewSpatial designDocId(String s) {
		reset();
		designDocId = s;
		return this;
	}

	public ViewSpatial listName(String s) {
		reset();
		listName = s;
		return this;
	}	
	/**
	 * Convenience manner of setting the bounding box using a string instead of
	 * accessing 4 settersTry to add checks for proper input some other time.
	 * @param s
	 * @return
	 */
	public ViewSpatial setBoundingBox(String s){
		/*String split[] = s.split(",", 4);
		long_lowerleft = Double.valueOf(split[3]);
		lat_lowerleft = Double.valueOf(split[2]);
		long_upperright = Double.valueOf(split[1]);
		lat_upperright = Double.valueOf(split[0]);
		if(long_lowerleft > long_upperright)
			throw new IllegalArgumentException(); 
		if(lat_lowerleft > lat_upperright)
			throw new IllegalArgumentException();*/		
		bboxpoints = s;
		return this;
	}
	
	public String bboxfourpoints(){
		return bboxpoints;
	}
	
/*	public double getLat_LowerLeft() {
		return lat_lowerleft;
	}
	public ViewSpatial setLat_LowerLeft(double lat1) {
		this.lat_lowerleft = lat1;
		return this;
	}
	public double getLong_LowerLeft() {
		return long_lowerleft;
	}
	public ViewSpatial setLong_LowerLeft(double long1) {
		this.long_lowerleft = long1;
		return this;
	}
	public double getLat_UpperRight() {
		return lat_upperright;
	}
	public ViewSpatial setLat_UpperRight(double lat2) {
		this.lat_upperright = lat2;
		return this;
	}
	public double getLong_UpperRight() {
		return long_upperright;
	}
	public ViewSpatial setLong_UpperRight(double long2) {
		this.long_upperright = long2;
		return this;
	}
*/	
	public String getPlanebounds() {
		return planebounds;
	}
	public ViewSpatial setPlanebounds(String planebounds) {
		this.planebounds = planebounds;
		return this;
	}

	/**
	 * limit=0 you don't get any data, but all meta-data for this View. The 
	 * number of documents in this View for example.
	 * @param i the limit
     * @return the view query for chained calls
	 */
	public ViewSpatial limit(int i) {
		reset();
		limit = i;
		return this;
	}
	/**
	 * The stale option can be used for higher performance at the cost of 
	 * possibly not seeing the all latest data. If you set the stale option to 
	 * ok, CouchDB may not perform any refreshing on the view that may be 
	 * necessary.
	 * @param b the staleOk flag
     * @return the view query for chained calls
	 */
	public ViewSpatial staleOk(boolean b) {
		reset();
		if(b)
			staleOk = "ok";
		else
			staleOk = null;
		return this;
	}

	/**
	 * The skip option should only be used with small values, as skipping a 
	 * large range of documents this way is inefficient (it scans the index from 
	 * the startkey and then skips N elements, but still needs to read all the 
	 * index values to do that). For efficient paging you'll need to use 
	 * startkey and limit. If you expect to have multiple documents emit 
	 * identical keys, you'll need to use startkey_docid in addition to startkey 
	 * to paginate correctly. The reason is that startkey alone will no longer 
	 * be sufficient to uniquely identify a row.
	 * @param i the skip count
     * @return the view query for chained calls
	 */
	public ViewSpatial skip(int i) {
		reset();
		skip = i;
		return this;
	}

	/**
	 * Resets internal state so this builder can be used again.
	 */
	public void reset() {
		cachedQuery = null;
	}

	public String buildQuery() {
		if (cachedQuery != null) {
			return cachedQuery;
		}
		URI query = buildViewPath();
		//first add bounding box and 4 points, then rest of possible params
		//if no bbox is provided we will get back an error for 1.1.x and
		//we will get back all docs in geocouch for couchdb 1.2.x
		if(this.bboxpoints !=null && this.bboxpoints.length()>0)
			query.param(boundingboxparameter, this.bboxfourpoints());
		else
			throw new IllegalStateException("A bounding box must be provided " +
					"for any spatial query.");
		//certain combinations of these parameters are non-sensical...
		//ONLY STALE & COUNT are supported by geocouch for couchdb 1.1.x
		//but it is ok to include the other paramters as they are merely
		//ignored if not supported
		if (count == true){
			query.param("count", "true");
			if (staleOk != null) {
				query.param("stale", staleOk);
			}
			if (planebounds != null && planebounds.length()>0){
				query.param("plane_bounds", planebounds);
			}
		} else {
			if (hasValue(limit)) {
				query.param("limit", limit);
			}
			if (staleOk != null) {
				query.param("stale", staleOk);
			}
			if (hasValue(skip)) {
				query.param("skip", skip);
			}
			if (planebounds != null && planebounds.length()>0){
				query.param("plane_bounds", planebounds);
			}
		}
		cachedQuery = query.toString();
		return cachedQuery;
	}

	private URI buildViewPath() {
		assertHasText(dbPath, "dbPath");
		URI uri = URI.of(dbPath);
		assertHasText(designDocId, "designDocId");
		// 							_spatial/ 				points
		uri.append(designDocId).append(spatialfunction).append(spatialfunctioname);
		return uri;
	}
	
	public String buildCompactSpatialIndexes() {
		URI compaction = buildCompactionPath();
		return compaction.toString();
	}
	
	private URI buildCompactionPath(){
		//Compaction of spatial indexes is per Design Document as per geocouch
		assertHasText(dbPath, "dbPath");
		URI uri = URI.of(dbPath);
		assertHasText(designDocId, "designDocId");
		///_compact' -H 'Content-Type: application/json'
		uri.append(designDocId).append(spatialfunction)
		.append("_compact -H 'Content-Type:application/json");
		return uri;
	}

	private void assertHasText(String s, String fieldName) {
		if (s == null || s.length() == 0) {
			throw new IllegalStateException(String.format("%s must have a value", fieldName));
		}
	}

	private boolean hasValue(int i) {
		return i != NOT_SET;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((cachedQuery == null) ? 0 : cachedQuery.hashCode());
		result = prime * result + ((dbPath == null) ? 0 : dbPath.hashCode());
		result = prime * result
				+ ((designDocId == null) ? 0 : designDocId.hashCode());
		result = prime * result + limit;
		result = prime * result + skip;
		result = prime * result + ((staleOk == null) ? 0 : staleOk.hashCode());
		result = prime * result + ((count == true) ? 1 : 0);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ViewSpatial other = (ViewSpatial) obj;
		if (cachedQuery == null) {
			if (other.cachedQuery != null)
				return false;
		} else if (!cachedQuery.equals(other.cachedQuery))
			return false;
		if (dbPath == null) {
			if (other.dbPath != null)
				return false;
		} else if (!dbPath.equals(other.dbPath))
			return false;
		if (designDocId == null) {
			if (other.designDocId != null)
				return false;
		} else if (!designDocId.equals(other.designDocId))
			return false;
		if (limit != other.limit)
			return false;
		if (listName == null) {
			if (other.listName != null)
				return false;
		} else if (!listName.equals(other.listName))
			return false;
		if (skip != other.skip)
			return false;
		if (staleOk == null) {
			if (other.staleOk != null)
				return false;
		} else if (!staleOk.equals(other.staleOk))
			return false;
		return true;
	}

	public boolean isIgnoreNotFound() {
		return ignoreNotFound;
	}
}
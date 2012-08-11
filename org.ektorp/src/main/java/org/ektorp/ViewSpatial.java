package org.ektorp;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.ListIterator;
import org.codehaus.jackson.map.ObjectMapper;
import org.ektorp.http.URI;
import org.ektorp.impl.StdObjectMapperFactory;
import org.ektorp.util.Assert;
/**
 * Improved support for geocouch. This class owes everything to the
 * ViewQuery class for its impetus and overall design. 
 * 
 * Supported only bounding box searches when first written, because that was all 
 * geocouch supported at the time. Now supports polygon searches as well, or 
 * bounding box searches. An enum is used and must be provided at some point
 * by client code. In future, perhaps radius and other search types can be added
 * to the enum, which would require more changes in this class.
 * @author Paul Torres
 *
 */
public class ViewSpatial {

  private final static ObjectMapper DEFAULT_MAPPER = 
      new StdObjectMapperFactory().createObjectMapper();
  private final static int NOT_SET = -1;
  private ObjectMapper mapper;
  private Spatial_type spatial_search_type;
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
  //just a place holder for final string representation of points
  private String bbox_or_poly_points = "";//"0,0,0,0";
  //keep track of points in our 2D coordinate plane
  private ArrayList<Point2D_customstring> points;
  //default plane-bounds for use with gps coordinates
  private String planebounds = null; //"-180,-90,180,90";	
  private String cachedQuery;
  private String listName;

  public ViewSpatial() {
    mapper = DEFAULT_MAPPER;
    init_common();
  }
  /**
   * Bring your own ObjectMapper.
   * The mapper is used when serializing keys when building the query.
   * @param om
   */
  public ViewSpatial(ObjectMapper om) {
    Assert.notNull(om, "ObjectMapper may not be null");
    mapper = om;
    init_common();
  }

  private void init_common(){
    points = new ArrayList<Point2D_customstring>(10);
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

  public ViewSpatial setSpatial_query_type(Spatial_type st) {
    reset();
    this.spatial_search_type = st;
    return this;
  }

  /**
   * Method to add a coordinate to the list of coordiante for use either with
   * a bounding box query or a polygon search query
   * @param a
   * @param b
   * @return
   */
  public ViewSpatial addPoint(double a, double b){
    this.points.add(new Point2D_customstring(a,b));
    return this;
  }
  /**
   * Method to add a coordinate to the list of coordiante for use either with
   * a bounding box query or a polygon search query
   * @param a
   * @param b
   * @return
   */
  public ViewSpatial addPoint(Point2D_customstring p){
    points.add(p);
    return this;
  }
  public ViewSpatial clearPoints(){
    points.clear();
    return this;
  } 
  /**
   * More convenient method to set the 2 points and not have to supply the 
   * enum.
   * @return
   */
  public ViewSpatial setBoundingBoxPoints(Point2D_customstring p, 
      Point2D_customstring p2){
    points.add(p);
    points.add(p2);
    this.spatial_search_type = Spatial_type.bbox;
    return this;
  }

  public String getPlanebounds() {
    return planebounds;
  }
  public ViewSpatial setPlanebounds(String plane) {
    planebounds = plane;
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
  public ViewSpatial setSkip(int i) {
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
  /**
   * Modified to support geocouch spatial queries, specifically polygon
   * searches and bounding box searches in a better way then before (just a
   * client provided string).
   * @return
   */
  public String buildQuery() {
    if (cachedQuery != null) {
      return cachedQuery;
    }
    URI query = buildViewPath();

    ListIterator<Point2D_customstring> li;
    Point2D_customstring point;
    StringBuilder sb;
    if(spatial_search_type == null) {
      throw new IllegalStateException("A stype of spatial search must be " +
          "specified.");
    }
    switch(spatial_search_type){
    case bbox:
      //ensure we have 2 points
      if(points.size()!=2){
        System.out.print(points.size());
        throw new IllegalStateException("Bounding box spatial search must be " +
            "provided with 2 points that define the box.");
      }
      li = points.listIterator();
      this.bbox_or_poly_points = "";
      sb = new StringBuilder();
      while(li.hasNext()){
        point = li.next();
        sb.append(point.getX());
        sb.append(",");
        sb.append(point.getY());
      }
      bbox_or_poly_points = sb.toString();
      query.param(spatial_search_type.get_type_as_String(), bbox_or_poly_points, this.spatial_search_type);		  
      break;

    case polygon:
      //any number of points is ok so long as the first and last are equal
      Point2D.Double first = points.get(0);
      Point2D.Double last = points.get(points.size()-1);
      if(first.getX() != last.getX() || first.getY()!= last.getY()){
        throw new IllegalStateException("Polygon searches require a set of " +
            "points that define the closed polygon.");
      }

      li = points.listIterator();
      bbox_or_poly_points = "";
      sb = new StringBuilder();
      while(li.hasNext()){
        sb.append(li.next().toString());
        if(li.hasNext()) sb.append(",");
      }
      bbox_or_poly_points = sb.toString();
      query.param("geometry", bbox_or_poly_points, this.spatial_search_type);
      break;
    default:
      throw new IllegalStateException("A type of spatial search must be " +
          "provided for any spatial query, such as POLYGON or BOUNDING BOX.");		
    }

    //first add bounding box and 4 points / polygon search points, then rest 
    //of possible params. If no bbox is provided we will get back an error 
    //for 1.1.x and we will get back all docs in geocouch for couchdb 1.2.x
/*if(this.bbox_or_poly_points !=null && this.bbox_or_poly_points.length()>0)
query.param(spatial_search_type.get_type_as_String(), this.spatial_query_points());
else
throw new IllegalStateException("A bounding box must be provided " +
		"for any spatial query.");
*/		//certain combinations of these parameters are non-sensical...
    //ONLY STALE & COUNT are supported by geocouch for couchdb 1.1.x
    //but it is ok to include the other paramters as they are merely
    //ignored if not supported. Support should be added in future but unsure
    //when
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
  
  public String buildCleanupSpatialIndexes() {
    URI cleanup = buildCleanupPath();
    return cleanup.toString();
  }
  /**
   * To cleanup spatial indexes that are no longer in use (this is per 
   * database): curl -X POST 'http://localhost:5984/places/_spatial_cleanup' -H 
   * 'Content-Type: application/json'
   * @return
   */
  private URI buildCleanupPath(){
    assertHasText(dbPath, "dbPath");
    URI uri = URI.of(dbPath)
    .append("_spatial_cleanup -H 'Content-Type:application/json");
    return uri;

  }

  private void assertHasText(String s, String fieldName) {
    if (s == null || s.length() == 0) {
      throw new IllegalStateException(
          String.format("%s must have a value", fieldName));
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
package org.ektorp;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Some very basic tests...need more. First bunch are really only for bbox. A
 * second bunch are for polygon searches. Still needs more testing...
 * @author Paul Torres
 *
 */
public class ViewSpatialTest {

  ViewSpatial spatial = new ViewSpatial()
  .dbPath("/somedb/")
  .designDocId("_design/doc")
  .setBoundingBoxPoints(new Point2D_customstring(0,0), 
      new Point2D_customstring(1,1));

  @Test(expected=java.lang.IllegalStateException.class)
  public void no_boundingbox_exception() {
    ViewSpatial spatiallocal = new ViewSpatial()
    .dbPath("/somedb/")
    .designDocId("_design/doc");
    spatiallocal.buildQuery();
  }


  @Test
  public void stale_ok_parameter_added() {
    String url = spatial
        .staleOk(true)
        //.setSpatial_query_type(Spatial_type.bbox)
        //.addPoint(new Point2D_customstring(1,1))
        //.addPoint(new Point2D_customstring(0,0))
        .buildQuery();
    assertTrue(contains(url, "&stale=ok"));
  }

  @Test
  public void stale_ok_set_to_false() {
    String url = spatial
        .staleOk(true)
        .staleOk(false)
        //.setSpatial_query_type(Spatial_type.bbox)
        //.addPoint(new Point2D_customstring(1,1))
        //.addPoint(new Point2D_customstring(0,0))
        .buildQuery();
    assertFalse(contains(url, "&stale=ok"));
  }

  @Test
  public void count_param_added() {
    String url = spatial
        .setCount(false).setCount(true)
        //.setSpatial_query_type(Spatial_type.bbox)
        //.addPoint(new Point2D_customstring(1,1))
        //.addPoint(new Point2D_customstring(0,0))
        .buildQuery();
    assertTrue(contains(url, "&count=true"));
  }
  @Test
  public void count_param_removed() {
    String url = spatial
        .setCount(true).setCount(false)
        //.setSpatial_query_type(Spatial_type.bbox)
        //.addPoint(new Point2D_customstring(1,1))
        //.addPoint(new Point2D_customstring(0,0))
        .buildQuery();
    assertFalse(contains(url, "&count=true"));
  }

  @Test
  public void planebounds_added() {
    String url = spatial
        .setPlanebounds("-180,-90,180,90")
        //.setSpatial_query_type(Spatial_type.bbox)
        //.addPoint(new Point2D_customstring(1,1))
        //.addPoint(new Point2D_customstring(0,0))
        .buildQuery();
    System.out.println(url);
    assertTrue(contains(url, "&plane_bounds=-180%2C-90%2C180%2C90"));
  }

  @Test
  public void planebounds_removed() {
    String url = spatial
        .setPlanebounds("-180,-90,180,90")
        .setPlanebounds("")
        //.setSpatial_query_type(Spatial_type.bbox)
        //.addPoint(new Point2D_customstring(1,1))
        //.addPoint(new Point2D_customstring(0,0))
        .buildQuery();
    System.out.println(url);
    assertFalse(contains(url, "&plane_bounds=-180%2C-90%2C180%2C90"));
  }
  /**
   * Points do not close the polygon 
   */
  @Test(expected=java.lang.IllegalStateException.class)
  public void setOpenPolygon() {
    String url = spatial
        .setSpatial_query_type(Spatial_type.polygon)
        .clearPoints()
        .addPoint(new Point2D_customstring(1,1))
        .addPoint(new Point2D_customstring(1,-1))
        .addPoint(new Point2D_customstring(-1,-1))
        .addPoint(new Point2D_customstring(-1,1))
        .buildQuery();
    System.out.println(url);
    assertTrue(contains(url, "&geometry=POLYGON((1+1,1+-1,-1+-1,-1+1))"));
  }
  /**
   * Non-sensical state; bbox with too many points
   */
  @Test(expected=java.lang.IllegalStateException.class)
  public void setTooManyParams() {
    String url = spatial
        .setSpatial_query_type(Spatial_type.bbox)
        //.clearPoints()
        .addPoint(new Point2D_customstring(1,1))
        .addPoint(new Point2D_customstring(1,-1))
        .addPoint(new Point2D_customstring(-1,-1))
        .addPoint(new Point2D_customstring(-1,1))
        .buildQuery();
    System.out.println(url);
  }
  
  /**
   * Define a closed polygon of 4 corners with 5 points, the 5th and last one
   * must be the same as the first one.
   */
  @Test
  public void setPolygon() {
    String url = spatial
        .setSpatial_query_type(Spatial_type.polygon)
        .clearPoints()
        .addPoint(new Point2D_customstring(1,1))
        .addPoint(new Point2D_customstring(1,-1))
        .addPoint(new Point2D_customstring(-1,-1))
        .addPoint(new Point2D_customstring(-1,1))
        .addPoint(new Point2D_customstring(1,1))
        .buildQuery();
    System.out.println(url);
    //( -> %28       ) -> %29       , -> %2C
    assertTrue(contains(url, "?geometry=POLYGON%28%281.0+1.0%2C1.0+-1.0%2C-1.0" +
    		"+-1.0%2C-1.0+1.0%2C1.0+1.0%29%29"));
  }


  private boolean contains(String subject, String s) {
    return subject.indexOf(s) > -1;
  }

  @Test(expected=java.lang.IllegalStateException.class)
  public void throw_exception_when_dbName_is_missing() {
    new ViewSpatial()
    //			.dbPath("/somedb/")
    .designDocId("_design/doc")
    .buildQuery();
  }
  @Test(expected=java.lang.IllegalStateException.class)
  public void throw_exception_when_designDocId_is_missing() {
    new ViewSpatial()
    .dbPath("/somedb/")
    //			.designDocId("_design/doc")
    .buildQuery();
  }

}
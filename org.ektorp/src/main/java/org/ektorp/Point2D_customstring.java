package org.ektorp;

import java.awt.geom.Point2D;
/**
 * For convenience, we need a toString() that returns a formatted string
 * specific to the needs of geocouch and the polygon search in particular.
 * Bounding box queries on the other hand do not require this method at all, 
 * but merely a comma delimited list, 4 elements in length, that describe 2 
 * points, without any "+" symbols. 
 * @author Paul Torres
 *
 */
class Point2D_customstring extends Point2D.Double {

  private static final long serialVersionUID = 1L;

  Point2D_customstring(double a, double b){
    super(a,b);
  }
  /**
   * Returns a string in proper format for the polygon search of geocouch. 
   */
  @Override
  public String toString(){
    //had to be specific
    java.lang.Double X = new java.lang.Double(x);
    java.lang.Double Y = new java.lang.Double(y);
    StringBuilder sb = new StringBuilder();
    sb.append(X.toString());
    //spaces -> + by the URL encoder in the URI class
    sb.append(" ");
    sb.append(Y.toString());
    return sb.toString();
  }
}

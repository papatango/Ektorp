package org.ektorp;
/**
 * Support for the only 2 (thus far) types of spatial queries; either 
 * bounding box searches (which need 4 numbers specifying 2 points) or
 * polygon searches (followed by an arbitrary even length of numbers 
 * specifying an arbitrary number of points that describe a closed polygon).
 * Perhaps in future we can support other types...such as radius... 
 * @author Paul Torres
 *
 */
public enum Spatial_type {
  
  bbox("bbox"), polygon("POLYGON");
  
  String type; 
  
  Spatial_type(String s){
    type = s;
  }
  
  public String get_type_as_String(){
    return type;
  }

}

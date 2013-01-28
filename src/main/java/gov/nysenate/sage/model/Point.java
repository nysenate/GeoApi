package gov.nysenate.sage.model;

import java.awt.geom.Point2D;

//import com.thoughtworks.xstream.annotations.XStreamAlias;
//@XStreamAlias("point")

/**
 * @author Ken Zalewski
 */

public class Point extends Point2D.Double
{
  private String m_address;
  

  /**
   * Construct a Point using the provided latitude and longitude.
   *
   * @param lat the latitude as a Double number
   * @param lon the longitude as a Double number
   */
  public Point(double lat, double lon)
  {
    this(lat, lon, null);
  } // Point()
  

  /**
   * Construct a Point using the provided latitude, longitude, and address.
   *
   * @param lat the latitude as a Double number
   * @param lon the longitude as a Double number
   * @param address a postal address as a string
   */
  public Point(double lat, double lon, String address)
  {
    super(lat, lon);
    m_address = address;
  } // Point()

  
  /**
   * Retrieve the latitude associated with this Point.
   *
   * @return the latitude as a double-precision number
   */
  public double getLatitude()
  {
    return super.getX();
  } // getLatitude()


  /**
   * Retrieve the longitude associated with this Point.
   *
   * @return the longitude as a double-precision number
   */
  public double getLongitude()
  {
    return super.getY();
  } // getLongitude()


  /**
   * Retrieve the address associated with this Point.
   *
   * @return the address as a string
   */
  public String getAddress()
  {
    return m_address;
  } // getAddress()


  /**
   * Get the string representation of this Point.  The string representation
   * is of the form <latitude>,<longitude>
   *
   * @return the string representation of this Point
   */
  public String toString()
  {
    return getLatitude()+","+getLongitude();
  } // toString()


  /**
   * Get the JSON representation of this Point.  The JSON representation
   * is of the form [<latitude>,<longitude>]
   *
   * @return a string containing the JSON representation of this Point
   */
  public String toJson()
  {
    return "["+this.toString()+"]";
  } // toJson()
} // Point

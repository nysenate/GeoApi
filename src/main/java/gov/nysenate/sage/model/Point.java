package gov.nysenate.sage.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.google.gson.annotations.SerializedName;

/**
 * @author Ken Zalewski
 */

@XStreamAlias("point")
public class Point
{
  @XStreamAlias("lat")
  @SerializedName("lat")
  private double m_latitude;
  @XStreamAlias("lon")
  @SerializedName("lon")
  private double m_longitude;
  @XStreamAlias("address")
  @SerializedName("address")
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
    m_latitude = lat;
    m_longitude = lon;
    m_address = address;
  } // Point()

  
  /**
   * Retrieve the latitude associated with this Point.
   *
   * @return the latitude as a double-precision number
   */
  public double getLatitude()
  {
    return m_latitude;
  } // getLatitude()


  /**
   * Retrieve the longitude associated with this Point.
   *
   * @return the longitude as a double-precision number
   */
  public double getLongitude()
  {
    return m_longitude;
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

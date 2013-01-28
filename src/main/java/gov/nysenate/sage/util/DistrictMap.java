package gov.nysenate.sage.util;

import gov.nysenate.sage.adapter.GeoServer;
import gov.nysenate.sage.model.Point;
import gov.nysenate.sage.model.Polygon;
import gov.nysenate.sage.service.DistrictService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;


/**
 * @author Ken Zalewski
 *
 */
public class DistrictMap
{
  private DistrictService.TYPE m_distType = null;
  private int m_district = 0;
  private Polygon m_polygon = null;
  private Logger logger = Logger.getLogger(DistrictMap.class);


  /**
   * Constructor.
   *
   * @param distType The type of district (eg. SENATE, ASSEMBLY, etc)
   * @param district The district number
  */
  public DistrictMap(DistrictService.TYPE distType, int district)
  {
    m_distType = distType;
    m_district = district;
  } // DistrictMap()


  /**
   * Get the district number associated with this object.
   *
   * @return the district number associated with this object
   */
  public int getDistrict()
  {
    return m_district;
  } // getDistrict()


  /**
   * Clear out the list of coordinates for this district.
   */
  public void clearCoordinates()
  {
    m_polygon = null;
  } // clearCoordinates()


  /**
   * Load coordinates for this district by sending a request to GeoServer.
   *
   * @return true on success, false on failure
   */
  public boolean loadCoordinates()
  {
    if (m_polygon != null) {
      logger.error("Coordinates already loaded for district "+m_district+". Please use clearCoordinates() first.");
      return false;
    }

    try {
      GeoServer geosrv = new GeoServer();
      ArrayList<Point> points = geosrv.getDistrictBoundary(m_district, m_distType);
      m_polygon = new Polygon(points);
      return true;
    }
    catch (IOException ioEx) {
      logger.error("Unable to instantiate GeoServer: "+ioEx.getMessage());
      return false;
    }
  } // loadCoordinates()


  /**
   * Read coordinates for this district from the provided file.  Each line
   * of the file should be of the form: [latitude],[longitude]
   *
   * @param mapFile A file containing a list of coordinates
   * @return true on success, false on failure
   */
  public boolean readCoordinates(File mapFile)
  {
    if (m_polygon != null) {
      logger.error("Coordinates already loaded for district "+m_district+". Please use clearCoordinates() first.");
      return false;
    }

    try {
      for (String line : FileUtils.readLines(mapFile)) {
        String[] point = line.split(",");
        Double lat = Double.valueOf(point[0]);
        Double lon = Double.valueOf(point[1]);
        m_polygon.appendPoint(new Point(lat, lon));
      }
      return true;
    }
    catch (IOException ioEx) {
      logger.error("Unable to read coordinates from file ["+mapFile.toString()+"]");
      return false;
    }
  } // readCoordinates()


  /**
   * Dump the current list of coordinates for this district to a file.
   *
   * @param outFile the file to which coordinates will be written
   * @return true on success, false on failure
   */
  public boolean writeCoordinates(File outFile)
  {
    try {
      FileWriter fw = new FileWriter(outFile);
      fw.write(this.toString());
      fw.close();
      return true;
    }
    catch (IOException ioEx) {
      logger.error("Unable to save coordinates to file");
      return false;
    }
  } // writeCoordinates()


  /**
   * Dump the current list of coordinates for this district to a file as JSON.
   *
   * @param outFile the file to which coordinates will be written
   * @return true on success, false on failure
   */
  public boolean writeCoordinatesAsJson(File outFile)
  {
    try {
      FileWriter fw = new FileWriter(outFile);
      fw.write(this.toJson());
      fw.close();
      return true;
    }
    catch (IOException ioEx) {
      logger.error("Unable to save coordinates to file");
      return false;
    }
  } // writeCoordinatesAsJson()


  /**
   * Get the polygon that is used by this DistrictMap
   *
   * @return the polygon used by this DistrictMap, or null if no coordinate
   * data has been loaded
   */
  public Polygon getPolygon()
  {
    return m_polygon;
  } // getPolygon()


  /**
   * Get the string representation of this DistrictMap.
   *
   * @return a string that represents this DistrictMap
   */
  public String toString()
  {
    return m_polygon.toString();
  } // toString()


  /**
   * Get the JSON representation of this DistrictMap.
   *
   * @return a JSON string that represents this DistrictMap
   */
  public String toJson()
  {
    return m_polygon.toJson();
  } // toJson()

} // DistrictMap

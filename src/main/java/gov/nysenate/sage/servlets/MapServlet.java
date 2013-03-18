package gov.nysenate.sage.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.deprecated.districts.Senate;
import gov.nysenate.sage.util.Config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;


public class MapServlet extends HttpServlet
{
  private final Logger logger = Logger.getLogger(MapServlet.class);

  private ObjectMapper m_objMapper;
  private HashMap<Integer, Senate> m_districtData;
  private HashMap<Integer, Double[][]> m_mappingData;

  public void init(ServletConfig config)
  {
    m_objMapper = new ObjectMapper();

    // Get the data directory
    File dataDirectory = new File(Config.read("district_maps.dir"));
    if (dataDirectory.isDirectory()) {
      // Load the mapping data
      m_mappingData = readMappingData(dataDirectory);
    }
    else {
      logger.error("Invalid district_maps.dir config value: "+dataDirectory.getAbsolutePath()+"; mapping data can't be loaded.");
    }

    // Load the senator data
    m_districtData = loadDistrictData();
  } // init()


  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    int districtNumber = 0;
    String point_data = "[]";
    String office_data = "[]";
    String xParam = request.getParameter("x");
    String yParam = request.getParameter("y");
    String districtParam = request.getParameter("sd");

    try {
      districtNumber = Integer.parseInt(districtParam);
      if (m_districtData.containsKey(districtNumber)) {
        Senate district = m_districtData.get(districtNumber);
        office_data = m_objMapper.writeValueAsString(district.getSenator().getOffices());
      }
      else {
        logger.warn("Office data not found for district "+districtNumber);
      }

      if (m_mappingData.containsKey(districtNumber)) {
        Double[][] coordinates = m_mappingData.get(districtNumber);
        point_data = m_objMapper.writeValueAsString(coordinates);
      }
      else {
        logger.warn("Point data not found for district "+districtNumber);
      }

    }
    catch (NumberFormatException e) {
      logger.error("Invalid districtParam recieved: '"+districtParam+"'", e);
    }
    catch (IOException e) {
      logger.error("Could not serialize the office or mapper data for "+districtParam, e);
    }

    request.setAttribute("point_data", point_data);
    request.setAttribute("office_data", office_data);
    request.setAttribute("district", districtNumber);
    request.setAttribute("x", xParam == null ? 800 : xParam);
    request.setAttribute("y", xParam == null ? 600 : yParam);
    request.getRequestDispatcher("/maps.jsp").forward(request, response);
  } // doGet()


  private HashMap<Integer, Double[][]> readMappingData(File mapDir)
  {
    final Pattern distFilePattern = Pattern.compile("sd([0-9]{1,2})\\.json");
    HashMap<Integer, Double[][]> mapData = new HashMap<Integer, Double[][]>();

    for (File mappingFile : FileUtils.listFiles(mapDir, null, false)) {
      Matcher districtMatcher = distFilePattern.matcher(mappingFile.getName());
      if (districtMatcher.find()) {
        try {
          int districtNumber = Integer.parseInt(districtMatcher.group(1));
          mapData.put(districtNumber, m_objMapper.readValue(mappingFile, Double[][].class));
        }
        catch (IOException ioEx) {
          // TODO Auto-generated catch block
          logger.error("Skipping "+mappingFile.getAbsolutePath()+"; Unexpected IOException. ", ioEx);
        }
      }
      else {
        logger.error("Skipping "+mappingFile.getAbsolutePath()+"; invalid filename.");
      }
    }
    return mapData;
  } // readMappingData()


  private HashMap<Integer, Senate> loadDistrictData()
  {
    final Pattern distNamePattern = Pattern.compile("State Senate District ([0-9]{1,2})");
    HashMap<Integer, Senate> distData = new HashMap<Integer, Senate>();
    /*Connect db = new Connect();

    try {
      for (Senate senate : (List<Senate>)db.getObjects(Senate.class)) {
        Matcher districtMatcher = distNamePattern.matcher(senate.getDistrict());
        if (districtMatcher.find()) {
          int districtNumber = Integer.parseInt(districtMatcher.group(1));
          // getObjects doesn't recursively pull in data from joined tables. Do it here
          Senate fullSenate = (Senate)db.getObject(Senate.class, "district", senate.getDistrict());
          distData.put(districtNumber, fullSenate);
        }
        else {
          logger.info("Skipping '"+senate.getDistrict()+"'; invalid district name.");
        }
      }
    }
    catch (Exception e) {
      logger.error("Unable to load senator data from database", e);
    }
    finally {
      db.close();
      return distData;
    }             */
      return null;
  } // loadDistrictData()
}

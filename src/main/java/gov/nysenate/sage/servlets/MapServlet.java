package gov.nysenate.sage.servlets;

import gov.nysenate.sage.model.districts.Senate;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.Connect;

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
import org.codehaus.jackson.map.ObjectMapper;

public class MapServlet extends HttpServlet {
    private final Logger logger = Logger.getLogger(MapServlet.class);

    private ObjectMapper mapper;
    private HashMap<Integer, Senate> districtData;
    private HashMap<Integer, Double[][]> mappingData;

    private final Pattern districtFilePattern = Pattern.compile("sd([0-9]{1,2})\\.json");
    private final Pattern districtNamePattern = Pattern.compile("State Senate District ([0-9]{1,2})");

    public void init(ServletConfig config) {
        mapper = new ObjectMapper();
        districtData = new HashMap<Integer, Senate>();
        mappingData = new HashMap<Integer, Double[][]>();

        // Get the data directory
        File dataDirectory = new File(Config.read("geoapi_data"));
        if (dataDirectory.isDirectory()) {
            // Load the mapping data
            File mappingDataDirectory = new File(dataDirectory,"mapping");
            for (File mappingFile : FileUtils.listFiles(mappingDataDirectory, null, false)) {
                Matcher districtMatcher = districtFilePattern.matcher(mappingFile.getName());
                if (districtMatcher.find()) {
                    try {
                        int districtNumber = Integer.parseInt(districtMatcher.group(1));
                        mappingData.put(districtNumber, mapper.readValue(mappingFile, Double[][].class));
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        logger.error("Skipping "+mappingFile.getAbsolutePath()+"; Unexpected IOException. ",e );
                    }
                } else {
                    logger.error("Skipping "+mappingFile.getAbsolutePath()+"; invalid filename.");
                }
            }
        } else {
            logger.error("Invalid geoapi_data config value: "+dataDirectory.getAbsolutePath()+"; mapping data can't be loaded.");
        }

        // Load the senator data
        Connect db = new Connect();
        try {
            for(Senate senate : (List<Senate>)db.getObjects(Senate.class)) {
                Matcher districtMatcher = districtNamePattern.matcher(senate.getDistrict());
                if (districtMatcher.find()) {
                    int districtNumber = Integer.parseInt(districtMatcher.group(1));
                    // getObjects doesn't recursively pull in data from joined tables. Do it here
                    Senate fullSenate = (Senate)db.getObject(Senate.class, "district", senate.getDistrict());
                    districtData.put(districtNumber, fullSenate);
                } else {
                    logger.info("Skipping '"+senate.getDistrict()+"'; invalid district name.");
                }
            }
        } catch(Exception e) {
            logger.error("Unable to load senator data from database",e);
        } finally {
            db.close();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int districtNumber = 0;
        String point_data = "[]";
        String office_data = "[]";
        String xParam = request.getParameter("x");
        String yParam = request.getParameter("y");
        String districtParam = request.getParameter("sd");

        try {
            districtNumber = Integer.parseInt(districtParam);
            if (districtData.containsKey(districtNumber)) {
                Senate district = districtData.get(districtNumber);
                office_data = mapper.writeValueAsString(district.getSenator().getOffices());
            } else {
                logger.warn("Office data not found for district "+districtNumber);
            }

            if (mappingData.containsKey(districtNumber)) {
                Double[][] coordinates = mappingData.get(districtNumber);
                point_data = mapper.writeValueAsString(coordinates);
            } else {
                logger.warn("Point data not found for district "+districtNumber);
            }

        } catch (NumberFormatException e) {
            logger.error("Invalid districtParam recieved: '"+districtParam+"'", e);
        } catch (IOException e) {
            logger.error("Could not serialize the office or mapper data for "+districtParam, e);
        }

        request.setAttribute("point_data", point_data);
        request.setAttribute("office_data", office_data);
        request.setAttribute("district", districtNumber);
        request.setAttribute("x", xParam == null ? 800 : xParam);
        request.setAttribute("y", xParam == null ? 600 : yParam);
        request.getRequestDispatcher("/maps.jsp").forward(request, response);
    }

}

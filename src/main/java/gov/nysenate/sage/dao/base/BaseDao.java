package gov.nysenate.sage.dao.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.model.geo.GeometryTypes;
import gov.nysenate.sage.model.geo.Line;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.service.address.ParallelAddressService;
import gov.nysenate.sage.service.district.ParallelDistrictService;
import gov.nysenate.sage.service.geo.ParallelGeocodeService;
import gov.nysenate.sage.service.geo.ParallelRevGeocodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BaseDao
{
    private static Logger logger = LoggerFactory.getLogger(BaseDao.class);
    public JdbcTemplate geoApiJbdcTemplate;
    public NamedParameterJdbcTemplate geoApiNamedJbdcTemplate;
    public JdbcTemplate tigerJbdcTemplate;
    public NamedParameterJdbcTemplate tigerNamedJdbcTemplate;

    private ParallelDistrictService parallelDistrictService;
    private ParallelGeocodeService parallelGeocodeService;
    private ParallelRevGeocodeService parallelRevGeocodeService;
    private ParallelAddressService parallelAddressService;
    private DatabaseConfig databaseConfig;
    private Environment env;

    @Autowired
    public BaseDao(ParallelDistrictService parallelDistrictService, ParallelGeocodeService parallelGeocodeService,
                   ParallelRevGeocodeService parallelRevGeocodeService, ParallelAddressService parallelAddressService,
                   DatabaseConfig databaseConfig, Environment env)
    {
        this.databaseConfig = databaseConfig;
        this.geoApiJbdcTemplate = this.databaseConfig.geoApiJdbcTemplate();
        this.geoApiNamedJbdcTemplate = this.databaseConfig.geoApiNamedJdbcTemplate();
        this.tigerJbdcTemplate = this.databaseConfig.tigerJdbcTemplate();
        this.tigerNamedJdbcTemplate = this.databaseConfig.tigerNamedJdbcTemplate();
        this.parallelAddressService = parallelAddressService;
        this.parallelDistrictService = parallelDistrictService;
        this.parallelGeocodeService = parallelGeocodeService;
        this.parallelRevGeocodeService = parallelRevGeocodeService;
        this.env = env;
    }

    @PreDestroy
    public void destroy(){
        close();
    }

    /**
     * Retrieve polylines from a GeoJson result
     * @param jsonLines
     * @return
     */
    public static List<Line> getLinesFromJson(String jsonLines)
    {
        if (jsonLines != null && !jsonLines.isEmpty() && jsonLines != "null") {
            //logger.debug("jsonLines: " + jsonLines);
            List<Line> lines = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode mapNode = objectMapper.readTree(jsonLines);
                String type = mapNode.get("type").asText();
                GeometryTypes geoType = GeometryTypes.valueOf(type.toUpperCase());

                JsonNode coordinates = mapNode.get("coordinates");
                if (geoType.equals(GeometryTypes.LINESTRING)) {
                    List<Point> points = new ArrayList<>();
                    for (int i = 0; i < coordinates.size(); i++) {
                        points.add(new Point(coordinates.get(i).get(1).asDouble(), coordinates.get(i).get(0).asDouble()));
                    }
                    lines.add(new Line(points));
                }
                else if (geoType.equals(GeometryTypes.MULTILINESTRING)) {
                    for (int i = 0; i < coordinates.size(); i++) {
                        List<Point> points = new ArrayList<>();
                        JsonNode jsonLine = coordinates.get(i);
                        for (int j = 0; j < jsonLine.size(); j++) {
                            points.add(new Point(jsonLine.get(j).get(1).asDouble(), jsonLine.get(j).get(0).asDouble()));
                        }
                        lines.add(new Line(points));
                    }
                }
                else {
                    return null;
                }
                return lines;
            }
            catch (IOException ex) {
                logger.error("" + ex);
            }
        }
        return null;
    }

    public boolean close()
    {
        try {
            parallelDistrictService.shutdownThread();
            parallelGeocodeService.shutdownThread();
            parallelRevGeocodeService.shutdownThread();
            parallelAddressService.shutdownThread();
            logger.info("All data connections have closed successfully");

            return true;
        }
        catch (Exception ex) {
            logger.error("Failed to close data connections/threads!", ex);
        }
        return false;
    }

    public String getDistrictSchema() {
        return env.getDistrictsSchema();
    }

    public String getJobSchema() {
        return env.getJobSchema();
    }

    public String getPublicSchema() {
        return env.getPublicSchema();
    }

    public String getLogSchema() {
        return env.getLogSchema();
    }

    public String getCacheSchema() {
        return env.getCacheSchema();
    }

    public String getTigerSchema() {
        return env.getTigerSchema();
    }

    public String getTigerDataSchema() {
        return env.getTigerDataSchema();
    }

    public String getGeocoderPublicSchema() {
        return env.getGeocoderPublicSchema();
    }
}

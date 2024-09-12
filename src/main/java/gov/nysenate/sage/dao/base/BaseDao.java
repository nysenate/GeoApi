package gov.nysenate.sage.dao.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.model.geo.GeometryTypes;
import gov.nysenate.sage.model.geo.Line;
import gov.nysenate.sage.model.geo.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BaseDao {
    private static final Logger logger = LoggerFactory.getLogger(BaseDao.class);
    public final JdbcTemplate geoApiJbdcTemplate;
    public final NamedParameterJdbcTemplate geoApiNamedJbdcTemplate;
    public final JdbcTemplate tigerJbdcTemplate;
    public final NamedParameterJdbcTemplate tigerNamedJdbcTemplate;
    private final Environment env;

    @Autowired
    public BaseDao(DatabaseConfig databaseConfig, Environment env) {
        this.geoApiJbdcTemplate = databaseConfig.geoApiJdbcTemplate();
        this.geoApiNamedJbdcTemplate = databaseConfig.geoApiNamedJdbcTemplate();
        this.tigerJbdcTemplate = databaseConfig.tigerJdbcTemplate();
        this.tigerNamedJdbcTemplate = databaseConfig.tigerNamedJdbcTemplate();
        this.env = env;
    }

    /**
     * Retrieve polylines from a GeoJson result
     */
    public static List<Line> getLinesFromJson(String jsonLines) {
        if (jsonLines == null || jsonLines.isEmpty() || jsonLines.equals("null")) {
            return null;
        }
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
            logger.error("{}", String.valueOf(ex));
        }
        return null;
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
}

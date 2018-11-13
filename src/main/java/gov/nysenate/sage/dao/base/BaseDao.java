package gov.nysenate.sage.dao.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.listener.SageConfigurationListener;
import gov.nysenate.sage.model.geo.GeometryTypes;
import gov.nysenate.sage.model.geo.Line;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.service.address.ParallelAddressService;
import gov.nysenate.sage.service.district.ParallelDistrictService;
import gov.nysenate.sage.service.geo.ParallelGeocodeService;
import gov.nysenate.sage.service.geo.ParallelRevGeocodeService;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.DB;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Repository
public class BaseDao
{
    private static Logger logger = LoggerFactory.getLogger(BaseDao.class);
    /** Dependency instances */
    private Config config;
    private DB baseDB;
    private DB tigerDB;
    protected DataSource dataSource;
    protected DataSource tigerDataSource;
    Marker fatal = MarkerFactory.getMarker("FATAL");

    @Autowired ParallelDistrictService parallelDistrictService;
    @Autowired ParallelGeocodeService parallelGeocodeService;
    @Autowired ParallelRevGeocodeService parallelRevGeocodeService;
    @Autowired ParallelAddressService parallelAddressService;

    @Autowired
    public BaseDao()
    {
        try {
            SageConfigurationListener configurationListener = new SageConfigurationListener();
            this.config = new Config("app.properties", configurationListener);
            this.baseDB = new DB(this.config, "db");
            this.tigerDB = new DB(this.config, "tiger.db");
        }
        catch(ConfigurationException e) {
            throw new RuntimeException("Failed to connect to DB's");
        }
        this.dataSource = baseDB.getDataSource();
        this.tigerDataSource = tigerDB.getDataSource();
    }

    public QueryRunner getQueryRunner()
    {
        return new QueryRunner(this.dataSource);
    }

    public AsyncQueryRunner getAsyncQueryRunner(ExecutorService executorService)
    {
        return new AsyncQueryRunner(executorService, this.getQueryRunner());
    }

    public QueryRunner getTigerQueryRunner()
    {
        return new QueryRunner(this.tigerDataSource);
    }

    public AsyncQueryRunner getAsyncTigerQueryRunner(ExecutorService executorService)
    {
        return new AsyncQueryRunner(executorService, this.getTigerQueryRunner());
    }

    public Connection getConnection()
    {
        try {
            return this.dataSource.getConnection();
        }
        catch (SQLException ex) {
            logger.error(fatal, "" + ex);
        }
        return null;
    }

    public Connection getTigerConnection()
    {
        try {
            return this.tigerDataSource.getConnection();
        }
        catch (SQLException ex) {
            logger.error(fatal, "" + ex);
        }
        return null;
    }

    public void closeConnection(Connection connection)
    {
        try {
            if (connection != null && !connection.isClosed()){
                connection.close();
            }
        }
        catch (SQLException ex){
            logger.error(fatal, "Failed to close connection!", ex);
        }
    }

    /**
     * Some geocoder queries don't know when to call it quits. Call this method before a query
     * to set the given timeout. If the query does time out a SQLException will be thrown.
     * @param timeOutInMs
     * @return
     */
    protected void setTimeOut(Connection conn, QueryRunner run, int timeOutInMs) throws SQLException
    {
        String setTimeout = "SET statement_timeout TO " + timeOutInMs + ";";
        run.update(conn, setTimeout);
    }

    /**
     * It's a good idea to reset the timeout after the query is done.
     * @return
     */
    protected void resetTimeOut(Connection conn, QueryRunner run) throws SQLException
    {
        String setTimeout = "RESET statement_timeout;";
        run.update(conn, setTimeout);
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
            baseDB.getDataSource().purge();
            tigerDB.getDataSource().purge();
        }
        catch (Exception ex) {
            logger.error("Failed to purge data connections!", ex);
        }

        try {
            baseDB.getDataSource().close(true);
            tigerDB.getDataSource().close(true);

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

    public Config getConfig() {
        return config;
    }
}

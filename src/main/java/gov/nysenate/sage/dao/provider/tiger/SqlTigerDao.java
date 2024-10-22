package gov.nysenate.sage.dao.provider.tiger;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.geo.Line;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a query interface to the TigerGeocoder database. For documentation on the geocoder
 * and it's available commands, refer to the following documentation (link subject to change):
 * <a href="http://postgis.net/docs/Extras.html">...</a>
 */
@Repository
public class SqlTigerDao implements TigerDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlTigerDao.class);
    private final BaseDao baseDao;

    @Autowired
    public SqlTigerDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    /**
     * Retrieves JSON geometry for a street that is matched in the set of zip5s.
     * @return GeoJSON string or null if no match.
     */
    private String getStreetLineGeometryAsJson(String streetName, List<Integer> zip5List) {
        if (zip5List == null || zip5List.isEmpty()) {
            return null;
        }
        String sql =
                """
                        WITH streets AS (
                          SELECT * FROM tiger_data.ny_edges edges
                          WHERE fullname ILIKE :streetName AND (%s)
                        )
                        SELECT fullname, \
                        ST_AsGeoJson(
                        ST_LineMerge(
                        (SELECT ST_Union(the_geom) FROM streets)
                        )
                        ) AS lines\s
                        FROM streets
                        GROUP BY fullname""";
        List<String> zip5WhereList = new ArrayList<>();
        for (Integer zip5 : zip5List) {
            zip5WhereList.add(String.format("(zipl %s OR zipr %s)", zip5, zip5));
        }
        String zip5Where = StringUtils.join(zip5WhereList, " OR ");
        sql = String.format(sql, zip5Where);
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("streetName", streetName);

            List<String> zip5QueryList = baseDao.tigerNamedJdbcTemplate.query(sql, params, new LineHandler());
            if (!zip5QueryList.isEmpty() && zip5QueryList.get(0) != null) {
                return zip5QueryList.get(0);
            }
        }
        catch (Exception ex) {
            logger.error("Failed to retrieve street line geometry!", ex);
        }
        return null;
    }

    /** {@inheritDoc} */
    public List<Line> getStreetLineGeometry(String streetName, List<Integer> zip5List) {
        String streetLineJson = getStreetLineGeometryAsJson(streetName, zip5List);
        if (streetLineJson != null) {
            return BaseDao.getLinesFromJson(streetLineJson);
        }
        return null;
    }

    private static class LineHandler implements RowMapper<String> {
        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("lines");
        }
    }
}
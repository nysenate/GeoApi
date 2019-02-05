package gov.nysenate.sage.dao.logger.point;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.logger.address.SqlAddressLogger;
import gov.nysenate.sage.model.geo.Point;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SqlPointLogger implements PointLogger
{
    private static Logger logger = LoggerFactory.getLogger(SqlAddressLogger.class);
    private BaseDao baseDao;

    @Autowired
    public SqlPointLogger(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    /** {@inheritDoc} */
    public int logPoint(Point point)
    {
        if (point != null) {
            int retrieveId = getPointId(point);
            if (retrieveId > 0) return retrieveId;

            try {

                MapSqlParameterSource params = new MapSqlParameterSource();
                params.addValue("latlon", "POINT (" + point.getLon() + " " + point.getLat() + ")");

                List<Integer> idList =  baseDao.geoApiNamedJbdcTemaplate.query(PointQuery.INSERT_POINT.getSql(baseDao.getLogSchema()),
                        params, new PointIdHandler());
                if (idList == null || idList.size() <= 0) {
                    return 0;
                }
                return idList.get(0);
            }
            catch (Exception ex) {
                logger.error("Failed to log point!", ex);
            }
        }
        return -1;
    }

    /** {@inheritDoc} */
    public int getPointId(Point point) {
        if (point != null) {
            try {

                MapSqlParameterSource params = new MapSqlParameterSource();
                params.addValue("latlon", "POINT (" + point.getLon() + " " + point.getLat() + ")");

                List<Integer> idList = baseDao.geoApiNamedJbdcTemaplate.query(PointQuery.GET_POINT_ID.getSql(baseDao.getLogSchema()),
                        params, new PointIdHandler());

                if (idList.size() == 0) {
                    return 0;
                }
                return idList.get(0);

            }
            catch (Exception ex) {
                logger.error("Failed to retrieve point id!", ex);
            }
        }
        return 0;
    }

    private static class PointIdHandler implements RowMapper<Integer> {
        @Override
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt("id");
        }
    }
}

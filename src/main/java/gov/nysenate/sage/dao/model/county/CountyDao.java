package gov.nysenate.sage.dao.model.county;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.district.County;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class CountyDao {
    private static final Logger logger = LoggerFactory.getLogger(CountyDao.class);
    private final BaseDao baseDao;
    private final List<County> counties;

    @Autowired
    public CountyDao(BaseDao baseDao) {
        this.baseDao = baseDao;
        this.counties = baseDao.geoApiNamedJbdcTemplate.query(
                CountyQuery.GET_ALL_COUNTIES.getSql(baseDao.getPublicSchema()), new CountyHandler());
    }

    public List<County> getCounties() {
        return counties;
    }

    public Integer getSenateCode(int fipsCode) {
        return counties.stream().filter(county -> county.fipsCode() == fipsCode)
                .map(County::senateCode).findFirst().orElse(null);
    }

    public County getCountyBySenateCode(int code) {
        try {
            var params = new MapSqlParameterSource("senateCode", code);
            List<County> countyList = baseDao.geoApiNamedJbdcTemplate
                    .query(CountyQuery.GET_COUNTY_BY_ID.getSql(baseDao.getPublicSchema()), params, new CountyHandler());
            if (countyList.get(0) != null) {
                return countyList.get(0);
            }
        }
        catch (Exception ex) {
            logger.error("Failed to get county by %s: %s%n%s".formatted("senateCode", code, ex.getMessage()));
        }
        return null;
    }

    private static class CountyHandler implements RowMapper<County> {
        @Override
        public County mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new County(rs.getInt("senate_code"), rs.getInt("fips_code"), rs.getInt("voterfile_code"),
                    rs.getString("name"), rs.getString("link"), rs.getString("streetfile_name"));
        }
    }
}

package gov.nysenate.sage.dao.model.county;

import com.google.common.collect.ImmutableList;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.district.County;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class CountyDao extends BaseDao {
    private static final Logger logger = LoggerFactory.getLogger(CountyDao.class);
    private ImmutableList<County> counties;

    @PostConstruct
    public void cacheCounties() {
        this.counties = ImmutableList.copyOf(namedJdbcTemplate.query(
                CountyQuery.GET_ALL_COUNTIES.getSql(getPublicSchema()), new CountyHandler()));
    }

    public List<County> getCounties() {
        return counties;
    }

    public String getSenateCodeStr(int fipsCode) {
        return counties.stream().filter(county -> county.fipsCode() == fipsCode)
                .map(county -> Integer.toString(county.senateCode())).findFirst().orElse(null);
    }

    public String getFipsCode(String senateCode) {
        return counties.stream().filter(county -> Integer.toString(county.senateCode()).equals(senateCode))
                .map(county -> Integer.toString(county.fipsCode())).findFirst().orElse(null);
    }

    public String getLinkBySenateCode(String senateCodeStr) {
        try {
            var params = new MapSqlParameterSource("senateCode", Integer.parseInt(senateCodeStr));
            return namedJdbcTemplate.queryForObject(
                    CountyQuery.GET_LINK_BY_SENATE_CODE.getSql(getPublicSchema()), params, String.class
            );
        }
        catch (Exception ex) {
            logger.error("Failed to get county by %s: %s%n%s".formatted("senateCode", senateCodeStr, ex.getMessage()));
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

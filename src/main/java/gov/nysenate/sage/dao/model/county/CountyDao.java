package gov.nysenate.sage.dao.model.county;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.model.district.DistrictTableInfo;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Repository
public class CountyDao extends BaseDao {
    public Set<County> getCounties(DistrictTableInfo countyInfo) {
        String sql = CountyQuery.GET_ALL_COUNTIES.getSql(getPublicSchema(), countyInfo.getReplacements("type"));
        return new HashSet<>(namedJdbcTemplate.query(sql, new CountyHandler()));
    }

    private static class CountyHandler implements RowMapper<County> {
        @Override
        public County mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new County(rs.getInt("code"), rs.getInt("voterfile_code"),
                    rs.getString("name"), rs.getString("link"));
        }
    }
}

package gov.nysenate.sage.dao.model.county;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.district.County;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Repository
public class CountyDao extends BaseDao {
    public Set<County> getCounties() {
        return new HashSet<>(
                namedJdbcTemplate.query(CountyQuery.GET_ALL_COUNTIES.getSql(getPublicSchema()), new CountyHandler())
        );
    }

    private static class CountyHandler implements RowMapper<County> {
        @Override
        public County mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new County(rs.getInt("senate_code"), rs.getInt("voterfile_code"),
                    rs.getString("name"), rs.getString("link"), rs.getString("streetfile_name"));
        }
    }
}

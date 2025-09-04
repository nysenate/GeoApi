package gov.nysenate.sage.dao.model.county;

import com.google.common.collect.ImmutableMap;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.district.County;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class CountyDao extends BaseDao {
    private ImmutableMap<String, County> countyMap;

    @PostConstruct
    public void cacheCounties() {
        this.countyMap = ImmutableMap.copyOf(
                namedJdbcTemplate.query(CountyQuery.GET_ALL_COUNTIES.getSql(getPublicSchema()), new CountyHandler())
                        .stream().collect(
                                Collectors.toMap(county -> String.valueOf(county.senateCode()), Function.identity())
                        )
        );
    }

    public County getCountyByCode(String code) {
        return countyMap.get(code);
    }

    private static class CountyHandler implements RowMapper<County> {
        @Override
        public County mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new County(rs.getInt("senate_code"), rs.getInt("voterfile_code"),
                    rs.getString("name"), rs.getString("link"), rs.getString("streetfile_name"));
        }
    }
}

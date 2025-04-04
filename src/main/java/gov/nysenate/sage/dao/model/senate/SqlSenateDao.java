package gov.nysenate.sage.dao.model.senate;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.services.model.District;
import gov.nysenate.services.model.Senator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SqlSenateDao extends BaseDao {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Inserts a Senator into the database.
     */
    public void insertOrReplaceSenator(Senator senator) {
        District district = senator.getDistrict();
        var params = new MapSqlParameterSource("district", district.getNumber())
                .addValue("url", district.getUrl())
                .addValue("name", senator.getName())
                .addValue("data", FormatUtil.toJsonString(senator));
        namedJdbcTemplate.update(SenateQuery.DELETE_SENATOR.getSql(getPublicSchema()), params);
        namedJdbcTemplate.update(SenateQuery.INSERT_SENATOR.getSql(getPublicSchema()), params);
    }

    /**
     * Gets all Senators, for caching.
     */
    public List<Senator> getSenators() {
        return namedJdbcTemplate.query(
                SenateQuery.GET_ALL_SENATORS.getSql(getPublicSchema()), new SenatorHandler());
    }

    /**
     * Retrieves senators from the database and puts Senator objects into the cache map.
     * The senator data is deserialized from the JSON representation of the Senator object
     * that is stored in the database.
     */
    private static class SenatorHandler implements RowMapper<Senator> {
        @Override
        public Senator mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            try {
                return mapper.readValue(rs.getString("data"), Senator.class);
            }
            catch (Exception ex) {
                throw new SQLException(ex.getMessage());
            }
        }
    }
}

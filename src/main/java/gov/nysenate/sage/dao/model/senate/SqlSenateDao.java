package gov.nysenate.sage.dao.model.senate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.services.model.District;
import gov.nysenate.services.model.Senator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

@Repository
public class SqlSenateDao extends BaseDao implements SenateDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlSenateDao.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private ImmutableMap<Integer, Senator> codeToSenatorMap;

    /** {@inheritDoc} */
    public Senator getSenatorByDistrict(int senateCode) {
        return codeToSenatorMap.get(senateCode);
    }

    /** {@inheritDoc} */
    public void insertSenator(Senator senator) {
        int senateCode = senator.getDistrict().getNumber();
        String senatorName = senator.getName();
        String json = FormatUtil.toJsonString(senator);

        try {
            var params = new MapSqlParameterSource("district", senateCode)
                    .addValue("name", senatorName)
                    .addValue("data", json);

            int numRows = namedJdbcTemplate.update(
                    SenateQuery.INSERT_SENATOR.getSql(getPublicSchema()), params);
            if (numRows > 0) {
                logger.info("Added data for Senator {}", senatorName);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    /** {@inheritDoc} */
    public void insertSenate(District district) {
        Integer senateCode = district.getNumber();
        String url = district.getUrl();

        try {
            var params = new MapSqlParameterSource("district", senateCode)
                    .addValue("url", url);
            int numRows = namedJdbcTemplate.update(
                    SenateQuery.INSERT_SENATE.getSql(getPublicSchema()), params);
            if (numRows > 0) {
                logger.info("Added data for senate district {}", senateCode);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    /** {@inheritDoc} */
    public void deleteSenators() {
        try {
            jdbcTemplate.update(SenateQuery.CLEAR_SENATORS.getSql(getPublicSchema()));
        } catch (Exception ex) {
            logger.error("Failed to delete senators {}", ex.getMessage());
        }
    }

    /** {@inheritDoc} */
    public void deleteSenator(int district) {
        try {
            var params = new MapSqlParameterSource("district", district);
            namedJdbcTemplate.update(
                    SenateQuery.DELETE_SENATOR_BY_DISTRICT.getSql(getPublicSchema()), params);
        } catch (Exception ex) {
            logger.error("Failed to delete senator in district {}", district);
        }
    }

    /** {@inheritDoc} */
    @PostConstruct
    @Scheduled(cron = "${senator.refresh.cron:0 0 0/12 * * *}")
    public void updateSenatorCache() {
        recreateSenatorCache();
    }

    private void recreateSenatorCache() {
        var tempMap = new HashMap<Integer, Senator>();
        List<Senator> uncompiledSenatorMap = namedJdbcTemplate.query(
                SenateQuery.GET_ALL_SENATORS.getSql(getPublicSchema()), new SenatorHandler());
        for (Senator senator : uncompiledSenatorMap) {
            tempMap.put(senator.getDistrict().getNumber(), senator);
        }
        codeToSenatorMap = ImmutableMap.copyOf(tempMap);
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

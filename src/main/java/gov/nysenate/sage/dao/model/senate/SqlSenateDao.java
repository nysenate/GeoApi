package gov.nysenate.sage.dao.model.senate;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SqlSenateDao extends BaseDao implements SenateDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlSenateDao.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private Map<Integer, Senator> codeToSenatorMap;

    @PostConstruct
    private void init() {
        this.codeToSenatorMap = queryForSenatorCache();
    }

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
            var params = new MapSqlParameterSource("district", senateCode);
            params.addValue("name", senatorName);
            params.addValue("data", json);

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
    @Scheduled(cron = "${senator.refresh.cron:0 0 0/12 * * *}")
    public void updateSenatorCache() {
        codeToSenatorMap = queryForSenatorCache();
    }

    private Map<Integer, Senator> queryForSenatorCache() {
        Map<Integer, Senator> senatorMap = new HashMap<>();
        try {
            List<Map<Integer, Senator>> uncompiledSenatorMap =
                    namedJdbcTemplate.query(
                            SenateQuery.GET_ALL_SENATORS.getSql(getPublicSchema()), new SenatorMapHandler());

            senatorMap = compileSenateMap(uncompiledSenatorMap);

        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return senatorMap;
    }

    private Map<Integer, Senator> compileSenateMap(List<Map<Integer, Senator>> uncompiledSenatorMap) {
        Map<Integer, Senator> compiledSenatorMap = new HashMap<>();
        for (Map<Integer, Senator> integerSenatorMap : uncompiledSenatorMap) {
            compiledSenatorMap.putAll(integerSenatorMap);
        }
        logger.info("Cached {} senators.", codeToSenatorMap.size());
        return compiledSenatorMap;
    }

    /**
     * Retrieves senators from the database and puts Senator objects into the cache map.
     * The senator data is deserialized from the JSON representation of the Senator object
     * that is stored in the database.
     */
    private class SenatorMapHandler implements RowMapper<Map<Integer, Senator>> {
        @Override
        public Map<Integer, Senator> mapRow(ResultSet rs, int rowNum) throws SQLException {
            Integer senateCode = rs.getInt("district");
            String name = rs.getString("name");
            String json = rs.getString("data");

            try {
                Senator senator = mapper.readValue(json, Senator.class);
                codeToSenatorMap.put(senateCode, senator);
            } catch (Exception ex) {
                logger.error("Failed to get senator data for {}", name, ex);
            }
            return codeToSenatorMap;
        }
    }
}

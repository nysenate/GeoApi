package gov.nysenate.sage.dao.model.senate;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.services.model.District;
import gov.nysenate.services.model.Senator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

@Repository
public class SqlSenateDao implements SenateDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlSenateDao.class);
    /**
     * Mapper used to serialize into json
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    private final BaseDao baseDao;
    private final Integer refreshIntervalHours;
    /**
     * Cached district code, Senator
     */
    private Map<Integer, Senator> senatorMap;
    private Timestamp cacheUpdated;

    @Autowired
    public SqlSenateDao(BaseDao baseDao,
                        @Value("${senator.cache.refresh.hours:12}") int refreshIntervalHours) {
        this.baseDao = baseDao;
        this.senatorMap = getSenatorMap();
        this.refreshIntervalHours = refreshIntervalHours;
    }

    /** {@inheritDoc} */
    public Collection<Senator> getSenators() {
        return getSenatorMap().values();
    }

    /** {@inheritDoc} */
    public Senator getSenatorByDistrict(int senateCode) {
        return getSenatorMap().get(senateCode);
    }

    /** {@inheritDoc} */
    public void insertSenator(Senator senator) {
        int senateCode = senator.getDistrict().getNumber();
        String senatorName = senator.getName();
        String json = FormatUtil.toJsonString(senator);

        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("district", senateCode);
            params.addValue("name", senatorName);
            params.addValue("data", json);

            int numRows = baseDao.geoApiNamedJbdcTemplate.update(
                    SenateQuery.INSERT_SENATOR.getSql(baseDao.getPublicSchema()), params);
            if (numRows > 0) {
                logger.info("Added data for Senator " + senatorName);
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
            int numRows = baseDao.geoApiNamedJbdcTemplate.update(
                    SenateQuery.INSERT_SENATE.getSql(baseDao.getPublicSchema()), params);
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
            baseDao.geoApiJbdcTemplate.update(SenateQuery.CLEAR_SENATORS.getSql(baseDao.getPublicSchema()));
        } catch (Exception ex) {
            logger.error("Failed to delete senators {}", ex.getMessage());
        }
    }

    /** {@inheritDoc} */
    public void deleteSenator(int district) {
        try {
            var params = new MapSqlParameterSource("district", district);
            baseDao.geoApiNamedJbdcTemplate.update(
                    SenateQuery.DELETE_SENATOR_BY_DISTRICT.getSql(baseDao.getPublicSchema()), params);
        } catch (Exception ex) {
            logger.error("Failed to delete senator in district {}", district);
        }
    }

    /** {@inheritDoc} */
    public void updateSenatorCache() {
        senatorMap = queryForSenatorCache();
    }

    private Map<Integer, Senator> queryForSenatorCache() {
        Map<Integer, Senator> senatorMap = new HashMap<>();
        try {
            List<Map<Integer, Senator>> uncompiledSenatorMap =
                    baseDao.geoApiJbdcTemplate.query(
                            SenateQuery.GET_ALL_SENATORS.getSql(baseDao.getPublicSchema()), new SenatorMapHandler());

            senatorMap = compileSenateMap(uncompiledSenatorMap);

        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return senatorMap;
    }

    private Map<Integer, Senator> getSenatorMap() {
        if (senatorMap == null || cacheUpdated == null || refreshIntervalElapsed()) {
            senatorMap = new HashMap<>();
            senatorMap = queryForSenatorCache();
        }
        return senatorMap;
    }

    private Map<Integer, Senator> compileSenateMap(List<Map<Integer, Senator>> uncompiledSenatorMap) {
        Map<Integer, Senator> compiledSenatorMap = new HashMap<>();
        for(int i=0; i < uncompiledSenatorMap.size(); i++) {
            compiledSenatorMap.putAll(uncompiledSenatorMap.get(i));
        }
        logger.info("Cached " + senatorMap.size() + " senators.");
        cacheUpdated = new Timestamp(new Date().getTime());
        return compiledSenatorMap;
    }

    /**
     * Indicates if it's time for a senator cache refresh.
     *
     * @return
     */
    private boolean refreshIntervalElapsed() {
        Timestamp now = new Timestamp(new Date().getTime());
        Timestamp refreshTime = new Timestamp(cacheUpdated.getTime() + (1000L * 3600 * refreshIntervalHours));
        return now.after(refreshTime);
    }

    /**
     * Retrieves senators from the database and puts Senator objects into the cache map.
     * The senator data is deserialzed from the JSON representation of the Senator object
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
                senatorMap.put(senateCode, senator);
            } catch (Exception ex) {
                logger.error("Failed to get senator data for {}", name, ex);
            }
            return senatorMap;
        }
    }


}

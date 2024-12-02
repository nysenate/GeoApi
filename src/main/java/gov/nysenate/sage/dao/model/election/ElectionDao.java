package gov.nysenate.sage.dao.model.election;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.district.ElectionOverlap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Repository
public class ElectionDao {
    private static final Logger logger = LoggerFactory.getLogger(ElectionDao.class);
    private final BaseDao baseDao;

    @Autowired
    public ElectionDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    // TODO: ~25% of entries don't have town_city. Better to not rely on it.
    public void rebuildMaps() {
        baseDao.geoApiNamedJbdcTemplate.update(ElectionQuery.CLEAR_TABLE.getSql(), Map.of());
        var handler = new ElectionCallbackHandler();
        baseDao.geoApiNamedJbdcTemplate.query(ElectionQuery.SELECT_ELECTION_DISTRICTS.getSql(), handler);
        logger.info("Got {} potential overlaps from the streetfile!", handler.overlapToDistricts.keySet().size());

        for (ElectionOverlap overlap : handler.overlapToDistricts.keySet()) {
            for (Integer electionDistrict : handler.overlapToDistricts.get(overlap)) {
                var params = new MapSqlParameterSource("congressionalId", overlap.congressionalId())
                        .addValue("countyFips", overlap.countyFips())
                        .addValue("senateId", overlap.senateId())
                        .addValue("assemblyId", overlap.assemblyId())
                        .addValue("gid", overlap.townCityId())
                        .addValue("electionId", electionDistrict);
                baseDao.geoApiNamedJbdcTemplate.update(ElectionQuery.INSERT_MAPS.getSql(), params);
            }
        }
        baseDao.geoApiNamedJbdcTemplate.update(ElectionQuery.REMOVE_EMPTY_MAPS.getSql(), Map.of());
    }

    private static class ElectionCallbackHandler implements RowCallbackHandler {
        private final Multimap<ElectionOverlap, Integer> overlapToDistricts = ArrayListMultimap.create();

        @Override
        public void processRow(@Nonnull ResultSet rs) throws SQLException {
            var currOverlap = new ElectionOverlap(rs.getInt("town_city_gid"),
                    rs.getInt("assembly_district"), rs.getInt("senate_district"), rs.getInt("county_fips_code"),
                    rs.getInt("congressional_district"));
            overlapToDistricts.put(currOverlap, rs.getInt("election_district"));
        }
    }
}

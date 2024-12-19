package gov.nysenate.sage.dao.model.election;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.provider.streetfile.SqlStreetfileDao;
import gov.nysenate.sage.model.district.ElectionOverlap;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.Geocoder;
import gov.nysenate.sage.service.geo.SageGeocodeServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ElectionDao {
    private static final Logger logger = LoggerFactory.getLogger(ElectionDao.class);
    private final BaseDao baseDao;
    private final SageGeocodeServiceProvider geocoder;
    private final SqlStreetfileDao streetfile;

    @Autowired
    public ElectionDao(BaseDao baseDao, SageGeocodeServiceProvider geocoder, SqlStreetfileDao streetfile) {
        this.baseDao = baseDao;
        this.geocoder = geocoder;
        this.streetfile = streetfile;
    }

    // TODO: ~25% of entries don't have town_city. Better to not rely on it.
    public void rebuildMaps() {
        baseDao.geoApiNamedJbdcTemplate.update(ElectionQuery.CLEAR_TABLE.getSql(), Map.of());
        var handler = new ElectionCallbackHandler();
        baseDao.geoApiNamedJbdcTemplate.query(ElectionQuery.SELECT_ELECTION_DISTRICTS.getSql(), handler);
        logger.info("Got {} potential overlaps from the streetfile!", handler.overlapToDistricts.keySet().size());

        for (ElectionOverlap overlap : handler.overlapToDistricts.keySet()) {
            Collection<Integer> electionDistricts = handler.overlapToDistricts.get(overlap);
            var params = new MapSqlParameterSource("congressionalId", overlap.congressionalId())
                    .addValue("countyFips", overlap.countyFips())
                    .addValue("senateId", overlap.senateId())
                    .addValue("assemblyId", overlap.assemblyId())
                    .addValue("townCityId", overlap.townCityId());
            boolean mapped = false;
            for (Integer electionDistrict : electionDistricts) {
                if (mapped) {
                    continue;
                }
                params.addValue("electionId", electionDistrict);
                baseDao.geoApiNamedJbdcTemplate.update(ElectionQuery.INSERT_MAPS.getSql(), params);
                mapped = true;

//                if (electionDistricts.size() == 1 || overlap.countyFips() != 1) {
//                    continue;
//                }
//                params.addValue("points", getPoints(electionDistrict, overlap));
//                logger.info("Got points for election district {}", electionDistricts);
//                baseDao.geoApiNamedJbdcTemplate.update(ElectionQuery.SEPARATE_ELECTION_DISTRICTS.getSql(), params);
            }
        }
        baseDao.geoApiNamedJbdcTemplate.update(ElectionQuery.REMOVE_EMPTY_MAPS.getSql(), Map.of());
        logger.info("Finished construction election district geometry!");
    }

    private String getPoints(int electionDistrict, ElectionOverlap overlap) {
        Set<String> points = geocoder.geocode(streetfile.getAddresses(electionDistrict, overlap),
                        List.of(Geocoder.GEOCACHE, Geocoder.NYSGEO), false).stream()
                .map(GeocodeResult::getGeocode).filter(Objects::nonNull)
                .filter(gc -> gc.quality() == GeocodeQuality.HOUSE || gc.quality() == GeocodeQuality.POINT)
                .map(Geocode::point).map(Point::specificToString)
                .collect(Collectors.toSet());
        return String.join(",", points);
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

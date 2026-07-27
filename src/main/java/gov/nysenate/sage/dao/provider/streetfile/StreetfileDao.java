package gov.nysenate.sage.dao.provider.streetfile;

import com.google.common.collect.ImmutableMap;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import gov.nysenate.sage.util.DistrictUtil;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.SqlTable;
import gov.nysenate.sage.model.Accuracy;
import gov.nysenate.sage.model.address.*;
import gov.nysenate.sage.model.district.*;
import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.scripts.streetfinder.model.StreetParity;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static gov.nysenate.sage.model.district.DistrictType.*;
import static gov.nysenate.sage.scripts.streetfinder.model.StreetParity.EVENS;
import static gov.nysenate.sage.scripts.streetfinder.model.StreetParity.ODDS;

@Repository
public class StreetfileDao extends BaseDao {
    private static final Logger logger = LoggerFactory.getLogger(StreetfileDao.class);
    public static final ImmutableMap<DistrictType, String> distColMap;
    private static final String copySqlTemplate = "COPY public.streetfile(%s) FROM STDIN CSV NULL '%s'";
    private final String columnOrder;
    private final BaseConnection connection;
    private boolean locked = false;

    // TODO: these should be dynamically generated
    static {
        var tempMap = new HashMap<DistrictType, String>();
        for (DistrictType type : List.of(CONGRESSIONAL, SENATE, ASSEMBLY, ELECTION, CITY_COUNCIL, MUNICIPAL_COURT)) {
            tempMap.put(type, type.name().toLowerCase() + "_district");
        }
        for (DistrictType type : List.of(COUNTY, COUNTY_LEGISLATURE, TOWN_CITY, WARD)) {
            tempMap.put(type, type.name().toLowerCase() + "_code");
        }
        tempMap.put(ZIP, "zip5");
        distColMap = ImmutableMap.copyOf(tempMap);
    }

    @Autowired
    public StreetfileDao(ComboPooledDataSource geoApiPostgresDataSource) throws SQLException {
        List<String> colList = new ArrayList<>(List.of("bldg_low", "bldg_high", "parity", "street", "postal_city", "zip5"));
        colList.addAll(order().stream().map(distColMap::get).toList());
        this.columnOrder = String.join(", ", colList);
        this.connection = geoApiPostgresDataSource.getConnection().unwrap(BaseConnection.class);
    }

    public String nullString() {
        return "NULL";
    }

    public List<DistrictType> order() {
        return distColMap.keySet().stream().filter(type -> type != DistrictType.ZIP).toList();
    }

    public void replaceStreetfile(Path streetfilePath) throws SQLException, IOException {
        checkLock();
        locked = true;
        jdbcTemplate.execute("TRUNCATE streetfile RESTART IDENTITY");
        var copyManager = new CopyManager(connection);
        copyManager.copyIn(copySqlTemplate.formatted(columnOrder, nullString()), new FileReader(streetfilePath.toFile()));
        locked = false;
    }

    /**
     * Performs a lookup in the streetfile table, consolidating districts if needed.
     * @return a districted address, with the highest possible match level.
     */
    public DistrictInfo getDistrictInfo(Address addr) {
        return getDistrictInfo(addr, Accuracy.HOUSE);
    }

    private DistrictInfo getDistrictInfo(Address addr, Accuracy accuracy) {
        logger.debug("Getting district info for {} at level {}", addr, accuracy);
        if (addr == null || accuracy == Accuracy.UNKNOWN || accuracy == null) {
            return DistrictInfo.empty;
        }
        var whereList = new ArrayList<String>();
        var params = new MapSqlParameterSource();
        if (addr.getPostalCity() != null) {
            whereList.add("postal_city = :postalCity");
            params.addValue("postalCity", addr.getPostalCity().toUpperCase());
        }
        if (addr.getZip5() != null) {
            whereList.add("zip5 = :zip5");
            params.addValue("zip5", addr.getZip5().toString());
        }
        if (addr instanceof BuildingAddress bldgAddr) {
            if (accuracy.compareTo(Accuracy.STREET) >= 0) {
                whereList.add("street = :street");
                params.addValue("street", bldgAddr.getStreet().toUpperCase());
            }
            if (accuracy == Accuracy.HOUSE) {
                int bldgNum;
                try {
                    bldgNum = Integer.parseInt(bldgAddr.getBldgId().replaceFirst("(?i)[a-z-]$", ""));
                } catch (NumberFormatException ex) {
                    logger.warn("Could not parse building number from {}", bldgAddr);
                    return getDistrictInfo(bldgAddr, accuracy.getNextHighestLevel());
                }
                StreetParity parity = bldgNum % 2 == 0 ? EVENS : ODDS;
                whereList.add("(bldg_low <= :bldgNum AND :bldgNum <= bldg_high)");
                params.addValue("bldgNum", bldgNum);
                whereList.add("(parity = 'ALL' OR parity = CAST(:parity AS parity))");
                params.addValue("parity", parity.name());
            }
        }
        if (whereList.isEmpty()) {
            return DistrictInfo.empty;
        }

        checkLock();
        String sql = "SELECT * FROM %s\n".formatted(SqlTable.STREETFILE) +
                " WHERE " + String.join(" AND ", whereList);
        List<DistrictedStreetRange> ranges = namedJdbcTemplate.query(sql, params, new DistrictStreetRangeMapper());
        if (ranges.isEmpty()) {
            return getDistrictInfo(addr, accuracy.getNextHighestLevel());
        }
        return DistrictUtil.getDistrictInfoWithoutConflicts(ranges.stream()
                .map(DistrictedStreetRange::districtInfo).toList(), accuracy);
    }

    /**
     * Returns a list of street ranges with district information for a given zip5.
     * @return List of DistrictedStreetRange
     */
    public List<DistrictedStreetRange> getRanges(Zip5 zip5) {
        if (zip5 == null) {
            return null;
        }

        checkLock();
        return namedJdbcTemplate.query(StreetfileQuery.SELECT_BY_ZIP.getSql(),
                        new MapSqlParameterSource("zip5", zip5.toString()), new DistrictStreetRangeMapper());
    }

    private void checkLock() {
        if (locked) {
            throw new RuntimeException("Streetfile is currently being reprocessed. Check back soon.");
        }
    }

    private static class DistrictStreetRangeMapper implements RowMapper<DistrictedStreetRange> {
        @Override
        public DistrictedStreetRange mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            var awn = new AddressWithoutNum(rs.getString("street"),
                    rs.getString("postal_city"), rs.getString("zip5"));
            var sar = new StreetAddressRange(rs.getInt("bldg_low"), rs.getInt("bldg_high"),
                    rs.getString("parity"), awn);
            var typeToDistrictMap = new HashMap<DistrictType, String>();
            for (DistrictType type : distColMap.keySet()) {
                String code = rs.getString(distColMap.get(type));
                if (code != null) {
                    typeToDistrictMap.put(type, code);
                }
            }
            return new DistrictedStreetRange(sar, new DistrictInfo(typeToDistrictMap, Accuracy.HOUSE));
        }
    }
}

package gov.nysenate.sage.dao.provider.streetfile;

import com.google.common.collect.ImmutableMap;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.*;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.scripts.streetfinder.model.StreetParity;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static gov.nysenate.sage.controller.api.DistrictUtil.consolidateDistrictInfo;
import static gov.nysenate.sage.model.district.DistrictType.*;
import static gov.nysenate.sage.scripts.streetfinder.model.StreetParity.EVENS;
import static gov.nysenate.sage.scripts.streetfinder.model.StreetParity.ODDS;

@Repository
public class SqlStreetfileDao implements StreetfileDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlStreetfileDao.class);
    private static final Map<DistrictType, String> distColMap;
    private static final String copySqlTemplate = "COPY public.streetfile(%s) FROM STDIN CSV NULL '%s'";
    private final BaseDao baseDao;
    private final String columnOrder;
    private final Connection connection;
    private boolean locked = false;

    static {
        var tempMap = new HashMap<DistrictType, String>();
        for (DistrictType type : List.of(CONGRESSIONAL, SENATE, ASSEMBLY, ELECTION, CITY_COUNCIL, MUNICIPAL_COURT)) {
            tempMap.put(type, type.name().toLowerCase() + "_district");
        }
        tempMap.put(COUNTY, "county_fips_code");
        tempMap.put(COUNTY_LEG, "county_leg_code");
        tempMap.put(TOWN_CITY, "town_city_gid");
        tempMap.put(WARD, "ward_code");
        tempMap.put(ZIP, "zip5");
        distColMap = ImmutableMap.copyOf(tempMap);
    }

    @Autowired
    public SqlStreetfileDao(BaseDao baseDao, ComboPooledDataSource geoApiPostgresDataSource) throws SQLException {
        this.baseDao = baseDao;
        List<String> colList = new ArrayList<>(List.of("bldg_low", "bldg_high", "parity", "street", "postal_city", "zip5"));
        colList.addAll(order().stream().map(distColMap::get).toList());
        this.columnOrder = String.join(", ", colList);
        this.connection = geoApiPostgresDataSource.getConnection().unwrap(BaseConnection.class);
    }

    @Override
    public String nullString() {
        return "null";
    }

    @Override
    public List<DistrictType> order() {
        return distColMap.keySet().stream().filter(type -> type != DistrictType.ZIP).toList();
    }

    @Override
    public void replaceStreetfile(Path streetfilePath) throws SQLException, IOException {
        checkLock();
        locked = true;
        baseDao.geoApiJbdcTemplate.execute("TRUNCATE streetfile RESTART IDENTITY");
        var copyManager = new CopyManager((BaseConnection) connection);
        copyManager.copyIn(copySqlTemplate.formatted(columnOrder, nullString()), new FileReader(streetfilePath.toFile()));
        locked = false;
    }

    public DistrictedAddress getDistrictedAddress(Address addr, @Nonnull DistrictMatchLevel matchLevel) {
        if (matchLevel == DistrictMatchLevel.NOMATCH || matchLevel == DistrictMatchLevel.STATE) {
            return new DistrictedAddress(new GeocodedAddress(addr), new DistrictInfo(), matchLevel);
        }
        AddressWithoutNum awn = AddressWithoutNum.fromAddress(addr);
        var sqlBuilder = new StringBuilder("SELECT * FROM streetfile WHERE postal_city = '%s'\n".formatted(awn.postalCity()));
        if (matchLevel.compareTo(DistrictMatchLevel.ZIP5) >= 0) {
            sqlBuilder.append("AND zip5 = %d\n".formatted(awn.zip5()));
        }
        if (matchLevel.compareTo(DistrictMatchLevel.STREET) >= 0) {
            sqlBuilder.append("AND street = '%s'".formatted(awn.street()));
        }
        if (matchLevel.compareTo(DistrictMatchLevel.HOUSE) >= 0) {
            int bldgNum;
            try {
                bldgNum = Integer.parseInt(addr.getStreetWithNum().replaceFirst(" .*$", ""));
            } catch (NumberFormatException ex) {
                logger.warn("Did not parse building number.");
                return getDistrictedAddress(addr, matchLevel.getNextHighestLevel());
            }
            StreetParity parity = bldgNum%2 == 0 ? EVENS : ODDS;
            sqlBuilder.append("AND (bldg_low <= %d AND %d <= bldg_high)".formatted(bldgNum, bldgNum))
                    .append("AND (parity = 'ALL' OR parity = '%s')".formatted(parity.name()));
        }

        checkLock();
        var ranges = baseDao.geoApiJbdcTemplate.query(sqlBuilder.toString(), new DistrictStreetRangeMapHandler());
        if (ranges == null || ranges.isEmpty()) {
            return getDistrictedAddress(addr, matchLevel.getNextHighestLevel());
        }
        DistrictInfo consolidatedInfo = consolidateDistrictInfo(ranges.values());
        return new DistrictedAddress(new GeocodedAddress(addr), consolidatedInfo, DistrictMatchLevel.ZIP5);
    }

    /** {@inheritDoc} */
    public List<DistrictedStreetRange> getDistrictStreetRangesByZip(Integer zip5) {
        return getDistrictStreetRanges("", List.of(zip5));
    }

    /** {@inheritDoc} */
    public List<DistrictedStreetRange> getDistrictStreetRanges(String street, List<Integer> zip5List) {
        // Short circuit the request under conditions where lots of data would be retrieved.
        if (zip5List == null || zip5List.isEmpty()) {
            return null;
        }
        else if (zip5List.size() > 1 && street.isEmpty()) {
            return null;
        }

        String sql =
            "SELECT * " +
            "FROM streetfile " +
            "WHERE CASE WHEN ? != '' THEN street = ? ELSE TRUE END " +
            "AND (%s) " +
            "ORDER BY street, bldg_low";

        List<String> zip5WhereList = new ArrayList<>();
        for (Integer zip5 : zip5List) {
            if (zip5 != null) {
                zip5WhereList.add(String.format("zip5 = '%d'", zip5));
            }
        }
        String zip5WhereSql = StringUtils.join(zip5WhereList, " OR ");
        sql = String.format(sql, zip5WhereSql);
        try {
            checkLock();
            Map<StreetAddressRange, DistrictInfo> resultMap =
                    baseDao.geoApiJbdcTemplate.query(sql, new DistrictStreetRangeMapHandler(), street, street);
            if (resultMap != null && !resultMap.isEmpty()) {
                List<DistrictedStreetRange> districtedStreetRanges = new ArrayList<>();
                for (StreetAddressRange sar : resultMap.keySet()) {
                    districtedStreetRanges.add(new DistrictedStreetRange(sar, resultMap.get(sar)));
                }
                return districtedStreetRanges;
            }
        }
        catch (Exception ex) {
            logger.error("Failed to get district street range lookup!", ex);
        }
        return null;
    }

    private void checkLock() {
        if (locked) {
            throw new RuntimeException("Streetfile is currently being reprocessed. Check back soon.");
        }
    }

    private static class DistrictStreetRangeMapHandler implements ResultSetExtractor<Map<StreetAddressRange,DistrictInfo>> {
        @Override
        public Map<StreetAddressRange, DistrictInfo> extractData(ResultSet rs) throws SQLException {
            Map<StreetAddressRange, DistrictInfo> streetRangeMap = new LinkedHashMap<>();
            while (rs.next()) {
                var awn = new AddressWithoutNum(rs.getString("street"),
                        rs.getString("postal_city"), rs.getInt("zip5"));
                var sar = new StreetAddressRange(rs.getInt("bldg_low"), rs.getInt("bldg_high"),
                        rs.getString("parity"), awn);

                var dInfo = new DistrictInfo();
                for (var type : distColMap.keySet()) {
                    dInfo.setDistCode(type, rs.getString(distColMap.get(type)));
                }
                streetRangeMap.put(sar, dInfo);
            }
            return streetRangeMap;
        }
    }
}

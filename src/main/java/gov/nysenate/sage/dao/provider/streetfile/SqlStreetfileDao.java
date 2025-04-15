package gov.nysenate.sage.dao.provider.streetfile;

import com.google.common.collect.ImmutableMap;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import gov.nysenate.sage.controller.api.DistrictUtil;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.SqlTable;
import gov.nysenate.sage.dao.provider.district.ShapefileDao;
import gov.nysenate.sage.model.address.BuildingAddress;
import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.address.StreetAddressRange;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.district.SingleDistrict;
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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gov.nysenate.sage.model.district.DistrictType.*;
import static gov.nysenate.sage.scripts.streetfinder.model.StreetParity.EVENS;
import static gov.nysenate.sage.scripts.streetfinder.model.StreetParity.ODDS;

@Repository
public class SqlStreetfileDao extends BaseDao implements StreetfileDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlStreetfileDao.class);
    private static final Map<DistrictType, String> distColMap;
    private static final String copySqlTemplate = "COPY public.streetfile(%s) FROM STDIN CSV NULL '%s'";
    private final String columnOrder;
    private final Connection connection;
    private final ShapefileDao shapefileDao;
    private boolean locked = false;

    static {
        var tempMap = new HashMap<DistrictType, String>();
        for (DistrictType type : List.of(CONGRESSIONAL, SENATE, ASSEMBLY, ELECTION, CITY_COUNCIL, MUNICIPAL_COURT)) {
            tempMap.put(type, type.name().toLowerCase() + "_district");
        }
        tempMap.put(COUNTY, "county_code");
        tempMap.put(COUNTY_LEG, "county_leg_code");
        tempMap.put(TOWN_CITY, "town_city_abbrev");
        tempMap.put(WARD, "ward_code");
        tempMap.put(ZIP, "zip5");
        distColMap = ImmutableMap.copyOf(tempMap);
    }

    @Autowired
    public SqlStreetfileDao(ComboPooledDataSource geoApiPostgresDataSource,
                            ShapefileDao shapefileDao) throws SQLException {
        List<String> colList = new ArrayList<>(List.of("bldg_low", "bldg_high", "parity", "street", "postal_city", "zip5"));
        colList.addAll(order().stream().map(distColMap::get).toList());
        this.columnOrder = String.join(", ", colList);
        this.connection = geoApiPostgresDataSource.getConnection().unwrap(BaseConnection.class);
        this.shapefileDao = shapefileDao;
    }

    @Override
    public String nullString() {
        return "NULL";
    }

    @Override
    public List<DistrictType> order() {
        return distColMap.keySet().stream().filter(type -> type != DistrictType.ZIP).toList();
    }

    @Override
    public void replaceStreetfile(Path streetfilePath) throws SQLException, IOException {
        checkLock();
        locked = true;
        jdbcTemplate.execute("TRUNCATE streetfile RESTART IDENTITY");
        var copyManager = new CopyManager((BaseConnection) connection);
        copyManager.copyIn(copySqlTemplate.formatted(columnOrder, nullString()), new FileReader(streetfilePath.toFile()));
        locked = false;
    }

    public DistrictInfo getDistrictInfo(BuildingAddress addr, @Nonnull DistrictMatchLevel matchLevel) {
        if (matchLevel == DistrictMatchLevel.NOMATCH) {
            return DistrictInfo.empty;
        }
        var sqlBuilder = new StringBuilder("SELECT * FROM %s WHERE postal_city = '%s'\n".formatted(SqlTable.STREETFILE, addr.getPostalCity().toUpperCase()));
        if (matchLevel.compareTo(DistrictMatchLevel.ZIP5) >= 0) {
            sqlBuilder.append(" AND zip5 = '%s'\n".formatted(addr.getZip5()));
        }
        if (matchLevel.compareTo(DistrictMatchLevel.STREET) >= 0) {
            sqlBuilder.append(" AND street = '%s'".formatted(addr.getStreet().toUpperCase()));
        }
        if (matchLevel.compareTo(DistrictMatchLevel.HOUSE) >= 0) {
            int bldgNum;
            try {
                bldgNum = Integer.parseInt(addr.getStreetWithNum().replaceFirst("(?i)[a-z]? .*$", ""));
            } catch (NumberFormatException ex) {
                logger.warn("Could not parse building number from {}", addr.getStreetWithNum());
                return getDistrictInfo(addr, matchLevel.getNextHighestLevel());
            }
            StreetParity parity = bldgNum%2 == 0 ? EVENS : ODDS;
            sqlBuilder.append(" AND (bldg_low <= %d AND %d <= bldg_high)".formatted(bldgNum, bldgNum))
                    .append( "AND (parity = 'ALL' OR parity = '%s')".formatted(parity.name()));
        }

        checkLock();
        List<DistrictedStreetRange> ranges = namedJdbcTemplate.query(sqlBuilder.toString(),
                new DistrictStreetRangeMapper());
        if (ranges.isEmpty()) {
            return getDistrictInfo(addr, matchLevel.getNextHighestLevel());
        }
        return DistrictUtil.getDistrictInfoWithoutConflicts(ranges.stream()
                .map(DistrictedStreetRange::districtInfo).toList(), matchLevel);
    }

    /** {@inheritDoc} */
    public List<DistrictedStreetRange> getDistrictStreetRangesByZip(Integer zip5) {
        if (zip5 == null) {
            return null;
        }

        checkLock();
        return namedJdbcTemplate.query(StreetfileQuery.SELECT_BY_ZIP.getSql(),
                        new MapSqlParameterSource("zip5", zip5), new DistrictStreetRangeMapper());
    }

    private void checkLock() {
        if (locked) {
            throw new RuntimeException("Streetfile is currently being reprocessed. Check back soon.");
        }
    }

    private class DistrictStreetRangeMapper implements RowMapper<DistrictedStreetRange> {
        @Override
        public DistrictedStreetRange mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            var awn = new AddressWithoutNum(rs.getString("street"),
                    rs.getString("postal_city"), rs.getString("zip5"));
            var sar = new StreetAddressRange(rs.getInt("bldg_low"), rs.getInt("bldg_high"),
                    rs.getString("parity"), awn);
            var typeToDistrictMap = new HashMap<DistrictType, SingleDistrict>();
            for (DistrictType type : distColMap.keySet()) {
                String code = rs.getString(distColMap.get(type));
                if (code != null) {
                    String name = shapefileDao.getDistrictName(type, code);
                    typeToDistrictMap.put(type, new SingleDistrict(code, name));
                }
            }
            return new DistrictedStreetRange(sar, new DistrictInfo(typeToDistrictMap, DistrictMatchLevel.HOUSE));
        }
    }
}

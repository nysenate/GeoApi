package gov.nysenate.sage.dao.provider.streetfile;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.*;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.scripts.streetfinder.model.StreetParity;
import gov.nysenate.sage.util.StreetAddressParser;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static gov.nysenate.sage.controller.api.DistrictUtil.consolidateDistrictInfo;
import static gov.nysenate.sage.model.district.DistrictType.*;
import static gov.nysenate.sage.scripts.streetfinder.model.StreetParity.EVENS;
import static gov.nysenate.sage.scripts.streetfinder.model.StreetParity.ODDS;

@Repository
public class SqlStreetfileDao implements StreetfileDao {
    private final Logger logger = LoggerFactory.getLogger(SqlStreetfileDao.class);
    private final BaseDao baseDao;

    private static final Map<DistrictType, String> distColMap = new HashMap<>();
    static {
        for (DistrictType type : List.of(CONGRESSIONAL, SENATE, ASSEMBLY, ELECTION, COUNTY_LEG, CITY_COUNCIL, MUNICIPAL_COURT)) {
            distColMap.put(type, type.name().toLowerCase() + "_district");
        }
        distColMap.put(COUNTY, "county_fips_code");
        distColMap.put(TOWN_CITY, "town_city_gid");
        distColMap.put(COUNTY_LEG, "county_leg_code");
        distColMap.put(WARD, "ward_code");
        distColMap.put(ZIP, "zip5");
    }

    @Autowired
    public SqlStreetfileDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    public DistrictedAddress getDistrictedAddress(Address addr, DistrictMatchLevel matchLevel) {
        if (matchLevel == null || matchLevel.compareTo(DistrictMatchLevel.CITY) < 0) {
            return null;
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
                bldgNum = Integer.parseInt(addr.getAddr1().replaceFirst(" .*$", ""));
            } catch (NumberFormatException ex) {
                return null;
            }
            StreetParity parity = bldgNum%2 == 0 ? EVENS : ODDS;
            sqlBuilder.append("AND (bldg_low <= %d AND %d <= bldg_high)".formatted(bldgNum, bldgNum))
                    .append("AND (parity = 'ALL' OR parity = '%s')".formatted(parity.name()));
        }

        var ranges = baseDao.geoApiJbdcTemplate.query(sqlBuilder.toString(), new DistrictStreetRangeMapHandler());
        if (ranges == null || ranges.isEmpty()) {
            return getDistrictedAddress(addr, matchLevel.getNextHighestLevel());
        }
        DistrictInfo consolidatedInfo = consolidateDistrictInfo(ranges.values());
        return new DistrictedAddress(new GeocodedAddress(addr), consolidatedInfo, DistrictMatchLevel.ZIP5);
    }

    /** {@inheritDoc} */
    public List<DistrictedStreetRange> getDistrictStreetRangesByZip(String zip5) {
        return getDistrictStreetRanges("", List.of(zip5));
    }

    /** {@inheritDoc} */
    public List<DistrictedStreetRange> getDistrictStreetRanges(String street, List<String> zip5List) {
        // Format the street name to aid in street file match
        street = (street != null) ? getFormattedStreet(street) : "";

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
        for (String zip5 : zip5List) {
            if (zip5 != null && !zip5.isEmpty()) {
                zip5WhereList.add(String.format("zip5 = '%s'", StringEscapeUtils.escapeSql(zip5)));
            }
        }
        String zip5WhereSql = StringUtils.join(zip5WhereList, " OR ");
        sql = String.format(sql, zip5WhereSql);
        try {
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

    private Map<DistrictType, Set<String>> extractMapResults(String sqlQuery) {
        return baseDao.geoApiJbdcTemplate.query(sqlQuery, rs -> {
            Map<DistrictType, Set<String>> resultMap = new HashMap<>();
            while (rs.next()) {
                DistrictType type = DistrictType.resolveType(rs.getString("type"));
                String code = rs.getString("code");
                if (!resultMap.containsKey(type)) {
                    resultMap.put(type, new HashSet<>());
                }
                resultMap.get(type).add(code);
            }
            return resultMap;
        });
    }

    /** {@inheritDoc} */
    public Map<DistrictType, Set<String>> getAllStandardDistrictMatches(List<String> streetList, List<String> zip5List) {
        // Short circuit on missing input
        if ((zip5List == null || zip5List.isEmpty()) && (streetList == null || streetList.isEmpty())) return null;

        String sqlTmpl = """
                SELECT DISTINCT %s::character varying AS code, '%s' AS type
                FROM streetfile
                WHERE (%s) AND (%s)""";
        // Create where clause for zip5 codes
        String zip5WhereSql = "TRUE";
        if (zip5List != null && !zip5List.isEmpty()) {
            List<String> zip5WhereList = new ArrayList<>();
            for (String zip5 : zip5List) {
                if (zip5 != null && !zip5.isEmpty()) {
                    zip5WhereList.add(String.format("'%s'", StringEscapeUtils.escapeSql(zip5)));
                }
            }
            zip5WhereSql = String.format("zip5 IN (%s)", StringUtils.join(zip5WhereList, ","));
        }

        // Create where clause for street names
        String streetWhereSql = "TRUE";
        if (streetList != null && !streetList.isEmpty()) {
            List<String> streetWhereList = new ArrayList<>();
            for (String stRaw : streetList) {
                String street = getFormattedStreet(stRaw);
                if (!street.isEmpty()) {
                    streetWhereList.add(String.format("'%s'", StringEscapeUtils.escapeSql(street)));
                }
            }
            if (!streetWhereList.isEmpty()) {
                streetWhereSql = String.format("street IN (%s)", StringUtils.join(streetWhereList, ","));
            }
        }

        List<String> queryList = new ArrayList<>();
        for (DistrictType dType : DistrictType.getStandardTypes()) {
            String column = distColMap.get(dType);
            String type = dType.name();
            queryList.add(String.format(sqlTmpl, column, type, zip5WhereSql, streetWhereSql));
        }
        String sqlQuery = StringUtils.join(queryList, " UNION ALL ");
        try {
            return extractMapResults(sqlQuery);
        }
        catch (Exception ex) {
            logger.error("Failed to get all possible state districts!", ex);
        }
        logger.info(sqlQuery);
        return null;
    }

    /** {@inheritDoc} */
    public Map<DistrictType, Set<String>> getAllIntersections(DistrictType distType, String sourceId) {
        if (distType == null || sourceId == null) {
            return null;
        }

        String sqlTmpl = """
                SELECT DISTINCT %s::character varying AS code, '%s' AS type
                FROM streetfile
                WHERE (%s)""";

        String districtSpec = distColMap.get(distType) + " = " + String.format("'%s'", sourceId);
        List<String> queryList = new ArrayList<>();
        for (DistrictType dType : DistrictType.getStandardTypes()) {
            String column = distColMap.get(dType);
            queryList.add(String.format(sqlTmpl, column, dType.name(), districtSpec));
        }
        String sqlQuery = StringUtils.join(queryList, " UNION ALL ");
        try {
            return extractMapResults(sqlQuery);
        }
        catch (Exception ex) {
            logger.error("Failed to get all possible state districts!", ex);
        }
        logger.info(sqlQuery);
        return null;
    }

    /**
     * Appends pre and post dirs to the street and upper cases the result.
     */
    private String getFormattedStreet(String street) {
        StreetAddress streetAddress = new StreetAddress();
        StreetAddressParser.extractStreet(street, streetAddress);
        StreetAddressParser.normalizeStreetAddress(streetAddress);
        return getFormattedStreet(streetAddress);
    }

    /**
     * Appends pre and post dirs to the street and upper cases the result.
     */
    private String getFormattedStreet(StreetAddress streetAddr) {
        if (streetAddr == null) {
            return "";
        }
        String street = (streetAddr.getPreDir() != null && !streetAddr.getPreDir().isEmpty()) ? streetAddr.getPreDir() + " " : "";
        street += streetAddr.getStreet();
        street += (streetAddr.getPostDir() != null && !streetAddr.getPostDir().isEmpty()) ? " " + streetAddr.getPostDir() : "";
        return street.toUpperCase();
    }

    private static class DistrictStreetRangeMapHandler implements ResultSetExtractor<Map<StreetAddressRange,DistrictInfo>> {
        @Override
        public Map<StreetAddressRange, DistrictInfo> extractData(ResultSet rs) throws SQLException {
            Map<StreetAddressRange, DistrictInfo> streetRangeMap = new LinkedHashMap<>();
            while (rs.next()) {
                var sar = new StreetAddressRange();

                sar.setId(rs.getInt("id"));
                sar.setBldgLoNum(rs.getInt("bldg_low"));
                sar.setBldgHiNum(rs.getInt("bldg_high"));
                sar.setBldgParity(rs.getString("parity"));
                sar.setStreet(rs.getString("street"));
                sar.setLocation(rs.getString("postal_city"));
                sar.setZip5(rs.getString("zip5"));

                var dInfo = new DistrictInfo();
                for (var type : DistrictType.values()) {
                    dInfo.setDistCode(type, rs.getString(distColMap.get(type)));
                }
                streetRangeMap.put(sar, dInfo);
            }
            return streetRangeMap;
        }
    }
}

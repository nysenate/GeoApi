package gov.nysenate.sage.dao.provider.streetfile;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.*;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
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

@Repository
public class SqlStreetfileDao implements StreetfileDao
{
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

    /** {@inheritDoc} */
    public Map<StreetAddressRange, DistrictInfo> getDistrictStreetRangeMap(
            StreetAddress streetAddr, boolean useStreet, boolean fuzzy, boolean useHouse) {
        var params = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder(512);
        sqlBuilder.append("SELECT * FROM streetfile WHERE 1=1 \n");

        boolean whereZip = (streetAddr.getZip5() != null && !streetAddr.getZip5().isEmpty());
        boolean whereStreet = (useStreet && streetAddr.getStreet() != null && !streetAddr.getStreet().isEmpty());
        boolean whereBldg = (useHouse && streetAddr.getBldgNum() != 0);
        boolean whereBldgChr = (useHouse && streetAddr.getBldgChar() != null && !streetAddr.getBldgChar().isEmpty());

        if (whereZip) {
            sqlBuilder.append(" AND zip5=? \n");
            params.add(Integer.valueOf(streetAddr.getZip5()));
        }

        if (whereStreet) {
            // Obtain a formatted street name with post/pre directionals applied
            String street = getFormattedStreet(streetAddr, false);

            // This street name is similar to the above except a matching prefix will be abbreviated.
            // i.e 'SAINT MARKS PL' -> 'ST MARKS PL'
            String streetPrefixAbbr = getFormattedStreet(streetAddr, true);

            // Sometimes the bldg_chr is actually the tail end of the street name
            if (whereBldgChr) {
                // Every one else gets a range check; sometimes the suffix is actually part of the street prefix.
                if (streetAddr.getBldgChar() != null) {
                    if (fuzzy) {
                        sqlBuilder.append(" AND (street LIKE ? OR (street LIKE ? AND (bldg_lo_chr='' OR bldg_lo_chr <= ?) AND (bldg_hi_chr='' OR ? <= bldg_hi_chr))) \n");
                        params.add(streetAddr.getBldgChar() + " " + street + "%");
                        params.add(street + "%");
                    }
                    else {
                        sqlBuilder.append(" AND (street = ? OR (street = ? AND (bldg_lo_chr='' OR bldg_lo_chr <= ?) AND (bldg_hi_chr='' OR ? <= bldg_hi_chr))) \n");
                        params.add(streetAddr.getBldgChar() + " " + street);
                        params.add(street);
                    }
                    params.add(streetAddr.getBldgChar());
                    params.add(streetAddr.getBldgChar());
                }
            }
            else {
                if (fuzzy) {
                    sqlBuilder.append(" AND (street LIKE ? OR street LIKE ?) \n");
                    params.add(street + "%");
                    params.add(streetPrefixAbbr + "%");
                }
                else {
                    sqlBuilder.append(" AND (street = ?) \n");
                    params.add(street);
                }
            }

            if (whereBldg) {
                sqlBuilder.append(" AND (bldg_lo_num <= ? AND ? <= bldg_hi_num AND (bldg_parity='ALL' or bldg_parity= ")
                        .append(streetAddr.getBldgNum() % 2 == 0 ? "'EVENS'" : "'ODDS'")
                        .append(")) \n");
                params.add(streetAddr.getBldgNum());
                params.add(streetAddr.getBldgNum());
            }
        }
        // Only do a lookup if we have meaningful filters on the query
        if (whereZip || whereStreet) {
            return baseDao.geoApiJbdcTemplate.query(sqlBuilder.toString(),
                    new DistrictStreetRangeMapHandler(), params.toArray());
        }
        else {
            logger.debug("Skipping address: no identifying information " + streetAddr);
            return null;
        }
    }

    /** {@inheritDoc} */
    public List<DistrictedStreetRange> getDistrictStreetRangesByZip(String zip5)
    {
        return getDistrictStreetRanges("", List.of(zip5));
    }

    /** {@inheritDoc} */
    public List<DistrictedStreetRange> getDistrictStreetRanges(String street, List<String> zip5List)
    {
        // Format the street name to aid in street file match
        street = (street != null) ? getFormattedStreet(street, false) : "";

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
    public Map<DistrictType, Set<String>> getAllStandardDistrictMatches(List<String> streetList, List<String> zip5List)
    {
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
                String street = getFormattedStreet(stRaw, false);
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
    public Map<DistrictType, Set<String>> getAllIntersections(DistrictType distType, String sourceId)
    {
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

    /** {@inheritDoc} */
    public DistrictedAddress getDistAddressByHouse(StreetAddress streetAddress) throws SQLException
    {
        return getDistAddressByHouse(streetAddress, false);
    }

    /** {@inheritDoc} */
    public DistrictedAddress getDistAddressByStreet(StreetAddress streetAddress) throws SQLException
    {
        return getDistAddressByStreet(streetAddress, false);
    }

    private DistrictedAddress getDistAddressByHouse(StreetAddress streetAddress, boolean useFuzzy) {
        Map<StreetAddressRange, DistrictInfo> rangeMap = getDistrictStreetRangeMap(streetAddress, true, useFuzzy, true);

        if (rangeMap == null ) {
            return null;
        }

        List<DistrictInfo> ranges = new ArrayList<>(rangeMap.values());
        DistrictInfo consolidatedDist = consolidateDistrictInfo(ranges);

        // If the consolidated dist returned null, we can either recurse with fuzzy on or return null
        if (consolidatedDist == null) {
            if (!useFuzzy) {
                return getDistAddressByHouse(streetAddress, true);
            }
            else {
                return null;
            }
        }
        else {
            return new DistrictedAddress(new GeocodedAddress(streetAddress.toAddress()), consolidatedDist, DistrictMatchLevel.HOUSE);
        }
    }

    private DistrictedAddress getDistAddressByStreet(StreetAddress streetAddress, boolean useFuzzy) {
        Map<StreetAddressRange, DistrictInfo> rangeMap = getDistrictStreetRangeMap(streetAddress, true, useFuzzy, false);

        if (rangeMap == null ) {
            return null;
        }

        List<DistrictInfo> ranges = new ArrayList<>(rangeMap.values());
        DistrictInfo consolidatedDist = consolidateDistrictInfo(ranges);

        // If the consolidated dist returned null, we can either recurse with fuzzy on or return null
        if (consolidatedDist == null ) {
            if (!useFuzzy) {
                return getDistAddressByStreet(streetAddress, true);
            }
            else {
                return null;
            }
        }
        else {
            return new DistrictedAddress(new GeocodedAddress(streetAddress.toAddress()), consolidatedDist, DistrictMatchLevel.STREET);
        }
    }

    /** {@inheritDoc} */
    public DistrictedAddress getDistAddressByZip(StreetAddress streetAddress) throws SQLException
    {
        Map<StreetAddressRange, DistrictInfo> rangeMap = getDistrictStreetRangeMap(streetAddress, false, false, false);

        if (rangeMap == null ) {
            return null;
        }

        List<DistrictInfo> ranges = new ArrayList<>(rangeMap.values());
        DistrictInfo consolidatedDist = consolidateDistrictInfo(ranges);
        if (consolidatedDist != null) {
            return new DistrictedAddress(new GeocodedAddress(streetAddress.toAddress()), consolidatedDist, DistrictMatchLevel.ZIP5);
        }
        else {
            return null;
        }
    }

    /**
     * Appends pre and post dirs to the street and upper cases the result.
     */
    private String getFormattedStreet(String street, boolean prefixNormalize)
    {
        StreetAddress streetAddress = new StreetAddress();
        StreetAddressParser.extractStreet(street, streetAddress);
        StreetAddressParser.normalizeStreetAddress(streetAddress);
        return getFormattedStreet(streetAddress, prefixNormalize);
    }

    /**
     * Appends pre and post dirs to the street and upper cases the result.
     */
    private String getFormattedStreet(StreetAddress streetAddr, boolean prefixNormalize) {
        if (streetAddr != null) {
            String street = (streetAddr.getPreDir() != null && !streetAddr.getPreDir().isEmpty()) ? streetAddr.getPreDir() + " " : "";
            street += (!prefixNormalize) ? streetAddr.getStreet() : StreetAddressParser.getPrefixNormalizedStreetName(streetAddr.getStreet());
            street += (streetAddr.getPostDir() != null && !streetAddr.getPostDir().isEmpty()) ? " " + streetAddr.getPostDir() : "";
            street = street.toUpperCase();
            return street;
        }
        return "";
    }

    public static class DistrictStreetRangeMapHandler implements ResultSetExtractor<Map<StreetAddressRange,DistrictInfo>>
    {
        @Override
        public Map<StreetAddressRange, DistrictInfo> extractData(ResultSet rs) throws SQLException
        {
            Map<StreetAddressRange, DistrictInfo> streetRangeMap = new LinkedHashMap<>();
            while (rs.next()) {
                StreetAddressRange sar = new StreetAddressRange();
                DistrictInfo dInfo = new DistrictInfo();

                sar.setId(rs.getInt("id"));
                sar.setBldgLoNum(rs.getInt("bldg_low"));
                sar.setBldgHiNum(rs.getInt("bldg_high"));
                sar.setStreet(rs.getString("street"));
                sar.setLocation(rs.getString("town"));
                sar.setZip5(rs.getString("zip5"));
                sar.setBldgParity(rs.getString("bldg_parity"));

                for (var type : DistrictType.values()) {
                    dInfo.setDistCode(type, rs.getString(distColMap.get(type)));
                }

                streetRangeMap.put(sar, dInfo);
            }
            return streetRangeMap;
        }
    }
}

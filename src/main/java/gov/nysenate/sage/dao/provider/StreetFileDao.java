package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.*;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.util.StreetAddressParser;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static gov.nysenate.sage.model.district.DistrictType.*;

@Repository
public class StreetFileDao
{
    private Logger logger = LoggerFactory.getLogger(StreetFileDao.class);
    private QueryRunner run;
    private BaseDao baseDao;

    private static Map<DistrictType, String> distColMap = new HashMap<>();
    static {
        distColMap.put(SENATE, "senate_code");
        distColMap.put(ASSEMBLY, "assembly_code");
        distColMap.put(CONGRESSIONAL, "congressional_code");
        distColMap.put(COUNTY, "county_code");
        distColMap.put(SCHOOL, "school_code");
        distColMap.put(TOWN, "town_code");
        distColMap.put(ELECTION, "election_code");
        distColMap.put(CLEG, "cleg_code");
        distColMap.put(CITY, "city_code");
        distColMap.put(FIRE, "fire_code");
        distColMap.put(VILLAGE, "vill_code");
        distColMap.put(WARD, "ward_code");
        distColMap.put(ZIP, "zip5");
    }

    @Autowired
    public StreetFileDao(BaseDao baseDao) {
        this.baseDao = baseDao;
        run = this.baseDao.getQueryRunner();
    }

    /**
     * Performs a street file lookup.
     * @param streetAddr     The StreetAddress to base the search on
     * @param useStreet      Use the street name as a criteria for the search
     * @param fuzzy          Use a wildcard on the street name to expand the search space
     * @param useHouse       Use the house number as a criteria for the search
     * @return               A LinkedHashMap containing StreetAddressRange and DistrictInfo
     * @throws SQLException
     */
    public Map<StreetAddressRange, DistrictInfo> getDistrictStreetRangeMap(
            StreetAddress streetAddr, boolean useStreet, boolean fuzzy, boolean useHouse) throws SQLException
    {
        ArrayList<Object> params = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder(512);
        sqlBuilder.append("SELECT * FROM streetfile WHERE 1=1 \n");

        boolean whereZip = (streetAddr.getZip5() != null && !streetAddr.getZip5().isEmpty());
        boolean whereState = (streetAddr.getState() != null && !streetAddr.getState().isEmpty());
        boolean whereStreet = (useStreet && streetAddr.getStreet() != null && !streetAddr.getStreet().isEmpty());
        boolean whereBldg = (useHouse && streetAddr.getBldgNum() != 0);
        boolean whereBldgChr = (useHouse && streetAddr.getBldgChar() != null && !streetAddr.getBldgChar().isEmpty());

        if (whereZip) {
            sqlBuilder.append(" AND zip5=? \n");
            params.add(Integer.valueOf(streetAddr.getZip5()));
        }

        if (whereState) {
            sqlBuilder.append(" AND state=? \n");
            params.add(streetAddr.getState());
        }

        if (whereStreet) {
            /** Obtain a formatted street name with post/pre directionals applied */
            String street = getFormattedStreet(streetAddr, false);

            /** This street name is similar to the above except a matching prefix will be abbreviated.
             *  i.e 'SAINT MARKS PL' -> 'ST MARKS PL' */
            String streetPrefixAbbr = getFormattedStreet(streetAddr, true);

            /** Sometimes the bldg_chr is actually the tail end of the street name */
            if (whereBldgChr) {
                /** Every one else gets a range check; sometimes the suffix is actually part of the street prefix. */
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
                /** Loose street match */
                if (fuzzy) {
                    sqlBuilder.append(" AND (street LIKE ? OR street LIKE ?) \n");
                    params.add(street + "%");
                    params.add(streetPrefixAbbr + "%");
                }
                /** Strict street match */
                else {
                    sqlBuilder.append(" AND (street = ?) \n");
                    params.add(street);
                }
            }

            if (whereBldg) {
                sqlBuilder.append(" AND (bldg_lo_num <= ? AND ? <= bldg_hi_num AND (bldg_parity='ALL' or bldg_parity= "
                        + (streetAddr.getBldgNum() % 2 == 0 ? "'EVENS'" : "'ODDS'") + ")) \n");
                params.add(streetAddr.getBldgNum());
                params.add(streetAddr.getBldgNum());
            }
        }
        /** Only do a lookup if we have meaningful filters on the query */
        if (whereZip || whereStreet) {
            return run.query(sqlBuilder.toString(), new DistrictStreetRangeMapHandler(), params.toArray());
        }
        else {
            logger.debug("Skipping address: no identifying information " + streetAddr);
            return null;
        }
    }

    /**
     * Returns a list of street ranges with district information for a given zip5.
     * @param zip5
     * @return List of DistrictedStreetRange
     */
    public List<DistrictedStreetRange> getDistrictStreetRangesByZip(String zip5)
    {
        return getDistrictStreetRanges("", Arrays.asList(zip5));
    }

    /**
     * Returns a list of street ranges with district information for a given street and zip5 list.
     * @param street
     * @param zip5List
     * @return List of DistrictedStreetRange
     */
    public List<DistrictedStreetRange> getDistrictStreetRanges(String street, List<String> zip5List)
    {
        /** Format the street name to aid in street file match */
        street = (street != null) ? getFormattedStreet(street, false) : "";

        /** Short circuit the request under conditions where lots of data would be retrieved. */
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
            "ORDER BY street, bldg_lo_num";

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
                    run.query(sql, new DistrictStreetRangeMapHandler(), street, street);
            if (resultMap != null && resultMap.size() > 0) {
                List<DistrictedStreetRange> districtedStreetRanges = new ArrayList<>();
                for (StreetAddressRange sar : resultMap.keySet()) {
                    districtedStreetRanges.add(new DistrictedStreetRange(sar, resultMap.get(sar)));
                }
                return districtedStreetRanges;
            }
        }
        catch (SQLException ex) {
            logger.error("Failed to get district street range lookup!", ex);
        }
        return null;
    }


    public Map<DistrictType, Set<String>> getAllStandardDistrictMatches(String zip5)
    {
        return getAllStandardDistrictMatches(null, Arrays.asList(zip5));
    }

    public Map<DistrictType, Set<String>> getAllStandardDistrictMatches(String street, String zip5)
    {
        return getAllStandardDistrictMatches(Arrays.asList(street), Arrays.asList(zip5));
    }

    public Map<DistrictType, Set<String>> getAllStandardDistrictMatches(List<String> zip5)
    {
        return getAllStandardDistrictMatches(null, zip5);
    }

    /**
     * Finds state district codes that overlap a given street/zip range.
     * @param streetList Optional street name
     * @param zip5List   Zip5 to match against
     * @return       A map of district types to a set of matched district codes.
     */
    public Map<DistrictType, Set<String>> getAllStandardDistrictMatches(List<String> streetList, List<String> zip5List)
    {
        /** Short circuit on missing input */
        if ((zip5List == null || zip5List.isEmpty()) && (streetList == null || streetList.isEmpty())) return null;

        String sqlTmpl = "SELECT DISTINCT %s::character varying AS code, '%s' AS type\n" +
                         "FROM streetfile\n" +
                         "WHERE (%s) AND (%s)";
        /** Create where clause for zip5 codes */
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

        /** Create where clause for street names */
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

        /** Format final query */
        List<String> queryList = new ArrayList<>();
        for (DistrictType dType : DistrictType.getStandardTypes()) {
            String column = distColMap.get(dType);
            String type = dType.name();
            queryList.add(String.format(sqlTmpl, column, type, zip5WhereSql, streetWhereSql));
        }
        String sqlQuery = StringUtils.join(queryList, " UNION ALL ");
        try {
            return run.query(sqlQuery, new ResultSetHandler<Map<DistrictType, Set<String>>>() {
                @Override
                public Map<DistrictType, Set<String>> handle(ResultSet rs) throws SQLException {
                    Map<DistrictType, Set<String>> resultMap = new HashMap<>();
                    while (rs.next()) {
                        DistrictType type = DistrictType.resolveType(rs.getString("type"));
                        String code = rs.getString("code");
                        if (!resultMap.containsKey(type)) {
                            resultMap.put(type, new HashSet<String>());
                        }
                        resultMap.get(type).add(code);
                    }
                    return resultMap;
                }
            });
        }
        catch (SQLException ex) {
            logger.error("Failed to get all possible state districts!", ex);
        }
        logger.info(sqlQuery);
        return null;
    }

    public DistrictedAddress getDistAddressByHouse(StreetAddress streetAddress) throws SQLException
    {
        return getDistAddressByHouse(streetAddress, false);
    }

    public DistrictedAddress getDistAddressByStreet(StreetAddress streetAddress) throws SQLException
    {
        return getDistAddressByStreet(streetAddress, false);
    }

    private DistrictedAddress getDistAddressByHouse(StreetAddress streetAddress, boolean useFuzzy) throws SQLException
    {
        Map<StreetAddressRange, DistrictInfo> rangeMap = getDistrictStreetRangeMap(streetAddress, true, useFuzzy, true);

        if (rangeMap == null ) {
            return null;
        }

        List<DistrictInfo> ranges = new ArrayList<>(rangeMap.values());
        DistrictInfo consolidatedDist = consolidateDistrictInfo(ranges);

        /** If the consolidated dist returned null, we can either recurse with fuzzy on or return null */
        if (consolidatedDist == null ) {
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

    private DistrictedAddress getDistAddressByStreet(StreetAddress streetAddress, boolean useFuzzy) throws SQLException
    {
        Map<StreetAddressRange, DistrictInfo> rangeMap = getDistrictStreetRangeMap(streetAddress, true, useFuzzy, false);

        if (rangeMap == null ) {
            return null;
        }

        List<DistrictInfo> ranges = new ArrayList<>(rangeMap.values());
        DistrictInfo consolidatedDist = consolidateDistrictInfo(ranges);

        /** If the consolidated dist returned null, we can either recurse with fuzzy on or return null */
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

    public DistrictedAddress getDistAddressByZip(StreetAddress streetAddress) throws SQLException
    {
        Map<StreetAddressRange, DistrictInfo> rangeMap = getDistrictStreetRangeMap(streetAddress, false, false, false);

        if (rangeMap == null ) {
            return null;
        }

        List<DistrictInfo> ranges = new ArrayList<>(rangeMap.values());
        DistrictInfo consolidatedDist = consolidateDistrictInfo(ranges);
        if (consolidatedDist != null){
            return new DistrictedAddress(new GeocodedAddress(streetAddress.toAddress()), consolidatedDist, DistrictMatchLevel.ZIP5);
        }
        else {
            return null;
        }
    }

    /**
     * Iterates over a list of DistrictInfo and returns a single DistrictInfo that represents the districts
     * that were common amongst every entry.
     * @param districtInfoList
     * @return DistrictInfo containing the districts that were common.
     *         If the senate code is not common, the return value will be null.
     */
    public DistrictInfo consolidateDistrictInfo(List<DistrictInfo> districtInfoList)
    {
        if (districtInfoList.size() == 0) return null;
        DistrictInfo baseDist = districtInfoList.get(0);

        for (int i= 1; i < districtInfoList.size(); i++) {
            DistrictInfo rangeDist = districtInfoList.get(i);

            /** Iterate through all district types and ensure that the districts in the base range are consistent
             *  with the current range. If a district has a mismatch, then the district code in the base range is nullified.
             */
            for (DistrictType distType : DistrictType.getAllTypes()) {

                String baseCode = baseDist.getDistCode(distType);
                String baseCounty = baseDist.getDistCode(COUNTY);
                String baseTown = baseDist.getDistCode(TOWN);
                String rangeCode = rangeDist.getDistCode(distType);
                String rangeCounty = rangeDist.getDistCode(COUNTY);
                String rangeTown = rangeDist.getDistCode(TOWN);
                boolean baseCodeValid = baseDist.hasDistrictCode(distType);
                boolean rangeCodeValid = rangeDist.hasDistrictCode(distType);
                boolean isCountyBased = DistrictType.getCountyBasedTypes().contains(distType);
                boolean isTownBased = DistrictType.getTownBasedTypes().contains(distType);

                if ( !(baseCodeValid && rangeCodeValid && rangeCode.equals(baseCode))
                                     || (isCountyBased && (rangeCounty == null || !rangeCounty.equals(baseCounty)))
                                     || (isTownBased && (rangeTown == null || !rangeTown.equals(baseTown)))) {
                    baseDist.setDistCode(distType, null);
                }
            }
        }

        if (baseDist.hasDistrictCode(SENATE)) {
            return baseDist;
        }
        else {
            return null;
        }
    }

    /**
     * Appends pre and post dirs to the street and upper cases the result.
     * @param street
     * @return
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
     * @param streetAddr
     * @return
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

    public static class DistrictStreetRangeMapHandler implements ResultSetHandler<Map<StreetAddressRange,DistrictInfo>>
    {
        @Override
        public Map<StreetAddressRange, DistrictInfo> handle(ResultSet rs) throws SQLException
        {
            Map<StreetAddressRange, DistrictInfo> streetRangeMap = new LinkedHashMap<>();
            while (rs.next()) {
                StreetAddressRange sar = new StreetAddressRange();
                DistrictInfo dInfo = new DistrictInfo();

                sar.setId(rs.getInt("id"));
                sar.setBldgLoNum(rs.getInt("bldg_lo_num"));
                sar.setBldgHiNum(rs.getInt("bldg_hi_num"));
                sar.setStreet(rs.getString("street"));
                sar.setLocation(rs.getString("town"));
                sar.setZip5(rs.getString("zip5"));
                sar.setBldgParity(rs.getString("bldg_parity"));

                dInfo.setDistCode(ELECTION, rs.getString(distColMap.get(ELECTION)));
                dInfo.setDistCode(COUNTY, rs.getString(distColMap.get(COUNTY)));
                dInfo.setDistCode(ASSEMBLY, rs.getString(distColMap.get(ASSEMBLY)));
                dInfo.setDistCode(SENATE, rs.getString(distColMap.get(SENATE)));
                dInfo.setDistCode(CONGRESSIONAL, rs.getString(distColMap.get(CONGRESSIONAL)));
                dInfo.setDistCode(TOWN, rs.getString(distColMap.get(TOWN)));
                dInfo.setDistCode(ZIP, rs.getString(distColMap.get(ZIP)));
                dInfo.setDistCode(WARD, rs.getString(distColMap.get(WARD)));
                dInfo.setDistCode(SCHOOL, rs.getString(distColMap.get(SCHOOL)));
                dInfo.setDistCode(CLEG, rs.getString(distColMap.get(CLEG)));
                dInfo.setDistCode(CITY, rs.getString(distColMap.get(CITY)));
                dInfo.setDistCode(VILLAGE, rs.getString(distColMap.get(VILLAGE)));
                dInfo.setDistCode(FIRE, rs.getString(distColMap.get(FIRE)));

                streetRangeMap.put(sar, dInfo);
            }
            return streetRangeMap;
        }
    }
}

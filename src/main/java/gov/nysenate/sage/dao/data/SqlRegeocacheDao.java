package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.NYSGeoAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SqlRegeocacheDao implements RegeocacheDao {

    private Logger logger = LoggerFactory.getLogger(SqlRegeocacheDao.class);
    private BaseDao baseDao;

    @Autowired
    public SqlRegeocacheDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    public List<Integer> determineMassGeocodeRecordCount(ArrayList<String> typeList) {
        String sql = RegeocacheQuery.MASS_GEOCACHE_COUNT.getSql(baseDao.getCacheSchema()) +
                getMassGeocacheBodySql(typeList);

        MapSqlParameterSource params = generateParamsForMassGeocache(typeList);
        if (params != null) {
            return baseDao.tigerNamedJdbcTemplate.query(sql, params, new CountRowMapper());
        }
        else {
            return baseDao.tigerNamedJdbcTemplate.query(sql, new CountRowMapper());
        }

    }

    public List<StreetAddress> getMassGeocodeBatch(int offset, int limit, ArrayList<String> typeList) {
        String sql = RegeocacheQuery.MASS_GEOCACHE_SELECT.getSql(baseDao.getCacheSchema()) + getMassGeocacheBodySql(typeList)
        + RegeocacheQuery.MASS_GEOCACHE_ORDER_BY.getSql() + RegeocacheQuery.MASS_GEOCACHE_LIMIT_OFFSET.getSql() ;

        MapSqlParameterSource params = generateParamsForMassGeocache(typeList);
        params.addValue("limit", limit);
        params.addValue("offset", offset);
        if (params != null) {
            return baseDao.tigerNamedJdbcTemplate.query(
                    sql, params,
                    new StreetAddressdRowMapper());
        }
        else {
            return baseDao.tigerNamedJdbcTemplate.query(sql, new StreetAddressdRowMapper());
        }
    }

    public Integer getNYSTotalAddresses() {
        return baseDao.tigerJbdcTemplate.queryForObject(
                RegeocacheQuery.NYS_COUNT_SQL.getSql(baseDao.getPublicSchema()), Integer.class);
    }

    public List<NYSGeoAddress> getBatchOfNYSGeoAddresses(int nys_limit, int nys_offset) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("limit", nys_limit);
        params.addValue("offset", nys_offset);
        return baseDao.tigerNamedJdbcTemplate.query(
                RegeocacheQuery.NYS_BATCH_SQL.getSql(baseDao.getPublicSchema()), params,
                new NysGeoAddressRowMapper());
    }

    public GeocodedStreetAddress getProviderOfAddressInCacheIfExists(StreetAddress nysStreetAddress) {
        MapSqlParameterSource geocacheParams = new MapSqlParameterSource();
        geocacheParams.addValue("bldgnum", nysStreetAddress.getBldgNum());
        geocacheParams.addValue("predir", nysStreetAddress.getPreDir());
        geocacheParams.addValue("street", nysStreetAddress.getStreetName());
        geocacheParams.addValue("postdir", nysStreetAddress.getPostDir());
        geocacheParams.addValue("streettype", nysStreetAddress.getStreetType());
        geocacheParams.addValue("zip5", nysStreetAddress.getZip5());
        geocacheParams.addValue("location", nysStreetAddress.getLocation());

        List<GeocodedStreetAddress> providerList = baseDao.tigerNamedJdbcTemplate
                .query(RegeocacheQuery.GEOCACHE_SELECT.getSql(
                        baseDao.getCacheSchema()), geocacheParams, new geocodedStreetAddressdRowMapper());

        if (providerList == null || providerList.isEmpty()) {
            return null;
        }
        return providerList.get(0);
    }

    public void insetIntoGeocache(StreetAddress nysStreetAddress, Geocode nysGeocode) throws SQLException {
        baseDao.tigerJbdcTemplate.update(RegeocacheQuery.INSERT_GEOCACHE.getSql(baseDao.getCacheSchema()),
                Integer.valueOf(nysStreetAddress.getBldgNum()),
                nysStreetAddress.getPreDir(), nysStreetAddress.getStreetName(),
                nysStreetAddress.getStreetType(),
                nysStreetAddress.getPostDir(), nysStreetAddress.getLocation(),
                nysStreetAddress.getState(), nysStreetAddress.getZip5(),
                "POINT(" + nysGeocode.getLon() + " " + nysGeocode.getLat() + ")",
                nysGeocode.getMethod(), nysGeocode.getQuality().name(), nysStreetAddress.getZip4());
    }

    public void updateGeocache(StreetAddress nysStreetAddress, Geocode nysGeocode) throws SQLException {
        baseDao.tigerJbdcTemplate.update(
                RegeocacheQuery.UPDATE_GEOCACHE.getSql(
                baseDao.getCacheSchema()),
                "POINT(" + nysGeocode.getLon() + " " + nysGeocode.getLat() + ")",
                nysGeocode.getMethod(),
                nysGeocode.getQuality().name(),
                nysStreetAddress.getZip4(),
                nysStreetAddress.getBldgNum(),
                nysStreetAddress.getStreetName(),
                nysStreetAddress.getStreetType(),
                nysStreetAddress.getPreDir(),
                nysStreetAddress.getPostDir(),
                nysStreetAddress.getZip5(),
                nysStreetAddress.getLocation()
        );
    }

    public List<String> getAllZips() {
        return baseDao.geoApiJbdcTemplate.query(
                RegeocacheQuery.SELECT_ZIPS.getSql(baseDao.getDistrictSchema()),
                (rs, rowNum) -> rs.getString("zcta5ce10"));
    }

    public List<NYSGeoAddress> getBatchOfNysGeoDups(int nys_limit, int nys_offset) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("limit", nys_limit);
        params.addValue("offset", nys_offset);
        return baseDao.tigerNamedJdbcTemplate.query(
                RegeocacheQuery.DUP_BATCH_SQL.getSql(baseDao.getPublicSchema()), params,
                new NysGeoAddressRowMapper());
    }

    public Integer getNYSTotalDupAddressesCount() {
        return baseDao.tigerJbdcTemplate.queryForObject(
                RegeocacheQuery.DUP_TOTAL_COUNT_SQL.getSql(baseDao.getPublicSchema()), Integer.class);
    }

    public List<Integer> getMethodTotalCount(String method) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("method", method);
        return baseDao.tigerNamedJdbcTemplate.query(
                RegeocacheQuery.METHOD_TOTAL_COUNT_SQL.getSql(baseDao.getCacheSchema()), params, new CountRowMapper());
    }

    public List<StreetAddress> getMethodBatch(int offset, int limit, String method) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("method", method);
        params.addValue("limit", limit);
        params.addValue("offset", offset);

        return baseDao.tigerNamedJdbcTemplate.query(
                RegeocacheQuery.METHOD_BATCH_SQL.getSql(baseDao.getCacheSchema()), params,
                new StreetAddressdRowMapper());
    }

    private String getMassGeocacheBodySql(ArrayList<String> typeList) {
        String generatedSql = "";
        for (int i = 0; i < typeList.size(); i = i+2) {
            switch (typeList.get(i)) {
                case "all":
                    //This is a special case for the mass geocache script. This overrides anything else sent in.
                    generatedSql = "";
                    return generatedSql;
                case "method":
                    if (i == 0) {
                        generatedSql = generatedSql + RegeocacheQuery.MASS_GEOCACHE_WHERE_METHOD.getSql();
                    }
                    else {
                        generatedSql = generatedSql + RegeocacheQuery.MASS_GEOCACHE_AND_METHOD.getSql();
                    }

                    break;
                case "town":
                    if (i == 0) {
                        generatedSql = generatedSql + RegeocacheQuery.MASS_GEOCACHE_WHERE_LOCATION.getSql();
                    }
                    else {
                        generatedSql = generatedSql + RegeocacheQuery.MASS_GEOCACHE_AND_LOCATION.getSql();
                    }
                    break;
                case "quality":
                    if (i == 0) {
                        generatedSql = generatedSql + RegeocacheQuery.MASS_GEOCACHE_WHERE_QUALITY.getSql();
                    }
                    else {
                        generatedSql = generatedSql + RegeocacheQuery.MASS_GEOCACHE_AND_QUALITY.getSql();
                    }
                    break;
                case "zipcode":
                    if (i == 0) {
                        generatedSql = generatedSql + RegeocacheQuery.MASS_GEOCACHE_WHERE_ZIP.getSql();
                    }
                    else {
                        generatedSql = generatedSql + RegeocacheQuery.MASS_GEOCACHE_AND_ZIP.getSql();
                    }
                    break;
            }
        }
        return generatedSql;
    }

    private MapSqlParameterSource generateParamsForMassGeocache(ArrayList<String> typeList) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        for (int i = 0; i < typeList.size(); i = i+2) {
            switch (typeList.get(i)) {
                case "all":
                    //This is a special case for the mass geocache script. This overrides anything else sent in.
                    return null;
                case "method":
                    params.addValue( "method", typeList.get(i+1));
                    break;
                case "town":
                    params.addValue("location" , typeList.get(i+1));
                    break;
                case "quality":
                    params.addValue( "quality", typeList.get(i+1));
                    break;
                case "zipcode":
                    params.addValue( "zipcode", typeList.get(i+1));
                    break;
            }
        }
        return params;
    }

    public static class NysGeoAddressRowMapper implements RowMapper<NYSGeoAddress> {

        @Override
        public NYSGeoAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
            NYSGeoAddress nysGeoAddress = new NYSGeoAddress();
            nysGeoAddress.setAddresslabel(rs.getString("addresslabel"));
            nysGeoAddress.setCitytownname(rs.getString("citytownname"));
            nysGeoAddress.setState(rs.getString("state"));
            nysGeoAddress.setZipcode(rs.getString("zipcode"));
            nysGeoAddress.setLatitude(rs.getDouble("latitude"));
            nysGeoAddress.setLongitude(rs.getDouble("longitude"));
            nysGeoAddress.setPointtype(rs.getInt("pointtype"));
            return nysGeoAddress;
        }
    }

    public static class geocodedStreetAddressdRowMapper implements RowMapper<GeocodedStreetAddress> {

        @Override
        public GeocodedStreetAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
            Geocode gc = new Geocode();
            gc.setMethod(rs.getString("method"));
            gc.setQuality(GeocodeQuality.valueOf(rs.getString("quality").toUpperCase()));
            StreetAddress sa = new StreetAddress();
            sa.setBldgNum(rs.getInt("bldgnum"));
            sa.setPreDir(rs.getString("predir"));
            sa.setStreetName(WordUtils.capitalizeFully(rs.getString("street")));
            sa.setStreetType(WordUtils.capitalizeFully(rs.getString("streettype")));
            sa.setPostDir(rs.getString("postdir"));
            sa.setLocation(WordUtils.capitalizeFully(rs.getString("location")));
            sa.setState(rs.getString("state"));
            sa.setZip5(rs.getString("zip5"));
            sa.setZip4(rs.getString("zip4"));
            return new GeocodedStreetAddress(sa, gc);
        }
    }

    public static class StreetAddressdRowMapper implements RowMapper<StreetAddress> {

        @Override
        public StreetAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
            StreetAddress streetAddress = new StreetAddress();
            streetAddress.setBldgNum(rs.getInt("bldgnum"));
            streetAddress.setPreDir(rs.getString("predir"));
            streetAddress.setStreetName(WordUtils.capitalizeFully(rs.getString("street")));
            streetAddress.setStreetType(WordUtils.capitalizeFully(rs.getString("streettype")));
            streetAddress.setPostDir(rs.getString("postdir"));
            streetAddress.setLocation(WordUtils.capitalizeFully(rs.getString("location")));
            streetAddress.setState(rs.getString("state"));
            streetAddress.setZip5(rs.getString("zip5"));
            streetAddress.setZip4(rs.getString("zip4"));
            return streetAddress;
        }
    }

    public static class CountRowMapper implements RowMapper<Integer> {

        @Override
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt("count");
        }
    }

}

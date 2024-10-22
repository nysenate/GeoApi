package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.NYSGeoAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import org.apache.commons.lang.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SqlRegeocacheDao implements RegeocacheDao {
    private final BaseDao baseDao;

    @Autowired
    public SqlRegeocacheDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    public List<Integer> determineMassGeocodeRecordCount(List<String> typeList) {
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

    public List<StreetAddress> getMassGeocodeBatch(int offset, int limit, List<String> typeList) {
        String sql = RegeocacheQuery.MASS_GEOCACHE_SELECT.getSql(baseDao.getCacheSchema()) + getMassGeocacheBodySql(typeList)
                + RegeocacheQuery.MASS_GEOCACHE_ORDER_BY.getSql() + RegeocacheQuery.MASS_GEOCACHE_LIMIT_OFFSET.getSql() ;

        MapSqlParameterSource params = generateParamsForMassGeocache(typeList)
                .addValue("limit", limit).addValue("offset", offset);
        return baseDao.tigerNamedJdbcTemplate.query(
                sql, params,
                new StreetAddressdRowMapper());
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
        MapSqlParameterSource geocacheParams = new MapSqlParameterSource()
                .addValue("bldgnum", nysStreetAddress.getBldgId())
                .addValue("street", nysStreetAddress.getStreet())
                .addValue("zip5", nysStreetAddress.getZip5())
                .addValue("location", nysStreetAddress.getPostalCity());

        List<GeocodedStreetAddress> providerList = baseDao.tigerNamedJdbcTemplate
                .query(RegeocacheQuery.GEOCACHE_SELECT.getSql(
                        baseDao.getCacheSchema()), geocacheParams, new geocodedStreetAddressdRowMapper());

        if (providerList.isEmpty()) {
            return null;
        }
        return providerList.get(0);
    }

    public void insetIntoGeocache(StreetAddress nysStreetAddress, Geocode nysGeocode) throws SQLException {
        baseDao.tigerJbdcTemplate.update(RegeocacheQuery.INSERT_GEOCACHE.getSql(baseDao.getCacheSchema()),
                nysStreetAddress.getBldgId(), nysStreetAddress.getStreet(),
                nysStreetAddress.getPostalCity(), nysStreetAddress.getZip5(),
                "POINT(" + nysGeocode.lon() + " " + nysGeocode.lat() + ")",
                nysGeocode.originalGeocoder(), nysGeocode.quality().name(), nysStreetAddress.getZip4());
    }

    public void updateGeocache(StreetAddress nysStreetAddress, Geocode nysGeocode) throws SQLException {
        baseDao.tigerJbdcTemplate.update(
                RegeocacheQuery.UPDATE_GEOCACHE.getSql(
                baseDao.getCacheSchema()),
                "POINT(" + nysGeocode.lon() + " " + nysGeocode.lat() + ")",
                nysGeocode.originalGeocoder(),
                nysGeocode.quality().name(),
                nysStreetAddress.getZip4(),
                nysStreetAddress.getBldgId(),
                nysStreetAddress.getStreet(),
                nysStreetAddress.getZip5(),
                nysStreetAddress.getPostalCity()
        );
    }

    public List<String> getAllZips() {
        return baseDao.geoApiJbdcTemplate.query(
                RegeocacheQuery.SELECT_ZIPS.getSql(baseDao.getDistrictSchema()),
                (rs, rowNum) -> rs.getString("zip_code"));
    }

    private String getMassGeocacheBodySql(List<String> typeList) {
        StringBuilder generatedSql = new StringBuilder();
        for (int i = 0; i < typeList.size(); i += 2) {
            switch (typeList.get(i)) {
                case "all":
                    //This is a special case for the mass geocache script. This overrides anything else sent in.
                    return "";
                case "method":
                    if (i == 0) {
                        generatedSql.append(RegeocacheQuery.MASS_GEOCACHE_WHERE_METHOD.getSql());
                    }
                    else {
                        generatedSql.append(RegeocacheQuery.MASS_GEOCACHE_AND_METHOD.getSql());
                    }

                    break;
                case "town":
                    if (i == 0) {
                        generatedSql.append(RegeocacheQuery.MASS_GEOCACHE_WHERE_LOCATION.getSql());
                    }
                    else {
                        generatedSql.append(RegeocacheQuery.MASS_GEOCACHE_AND_LOCATION.getSql());
                    }
                    break;
                case "quality":
                    if (i == 0) {
                        generatedSql.append(RegeocacheQuery.MASS_GEOCACHE_WHERE_QUALITY.getSql());
                    }
                    else {
                        generatedSql.append(RegeocacheQuery.MASS_GEOCACHE_AND_QUALITY.getSql());
                    }
                    break;
                case "zipcode":
                    if (i == 0) {
                        generatedSql.append(RegeocacheQuery.MASS_GEOCACHE_WHERE_ZIP.getSql());
                    }
                    else {
                        generatedSql.append(RegeocacheQuery.MASS_GEOCACHE_AND_ZIP.getSql());
                    }
                    break;
            }
        }
        return generatedSql.toString();
    }

    private static MapSqlParameterSource generateParamsForMassGeocache(List<String> typeList) {
        var params = new MapSqlParameterSource();
        for (int i = 0; i < typeList.size(); i = i + 2) {
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
            // TODO: why is Point not assigned?
            GeocodeQuality quality = GeocodeQuality.fromString(rs.getString("quality"));
            Geocode gc = new Geocode(new Point(0.0d, 0.0d), quality, rs.getString("method"));
            return new GeocodedStreetAddress(new StreetAddressdRowMapper().mapRow(rs, rowNum), gc);
        }
    }

    public static class StreetAddressdRowMapper implements RowMapper<StreetAddress> {
        @Override
        public StreetAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
            var awn = new AddressWithoutNum(WordUtils.capitalizeFully(rs.getString("street")),
                    WordUtils.capitalizeFully(rs.getString("location")),
                    rs.getInt("zip5"));
            var streetAddress = new StreetAddress(awn);
            streetAddress.setBldgId(rs.getString("bldgnum"));
            streetAddress.setZip4(rs.getInt("zip4"));
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

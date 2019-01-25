package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.NYSGeoAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Geocode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SqlRegeocacheDao implements RegeocacheDao {

    private Logger logger = LoggerFactory.getLogger(SqlRegeocacheDao.class);
    private BaseDao baseDao;

    @Autowired
    public SqlRegeocacheDao(BaseDao baseDao) {
        this.baseDao = baseDao;
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

    public String getProviderOfAddressInCacheIfExists(StreetAddress nysStreetAddress) {
        MapSqlParameterSource geocacheParams = new MapSqlParameterSource();
        geocacheParams.addValue("bldgnum", nysStreetAddress.getBldgNum());
        geocacheParams.addValue("predir", nysStreetAddress.getPreDir());
        geocacheParams.addValue("street", nysStreetAddress.getStreetName());
        geocacheParams.addValue("postdir", nysStreetAddress.getPostDir());
        geocacheParams.addValue("streettype", nysStreetAddress.getStreetType());
        geocacheParams.addValue("zip5", nysStreetAddress.getZip5().toString());
        geocacheParams.addValue("location", nysStreetAddress.getLocation());
        return baseDao.tigerNamedJdbcTemplate
                .queryForObject(
                        RegeocacheQuery.GEOCACHE_SELECT.getSql(baseDao.getCacheSchema()), geocacheParams, String.class);
    }

    public void insetIntoGeocache(StreetAddress nysStreetAddress, Geocode nysGeocode) {
        baseDao.tigerJbdcTemplate.update(RegeocacheQuery.INSERT_GEOCACHE.getSql(baseDao.getCacheSchema()),
                Integer.valueOf(nysStreetAddress.getBldgNum()),
                nysStreetAddress.getPreDir(), nysStreetAddress.getStreetName(),
                nysStreetAddress.getStreetType(),
                nysStreetAddress.getPostDir(), nysStreetAddress.getLocation(),
                nysStreetAddress.getState(), nysStreetAddress.getZip5(),
                "POINT(" + nysGeocode.getLon() + " " + nysGeocode.getLat() + ")",
                nysGeocode.getMethod(), nysGeocode.getQuality().name(), nysStreetAddress.getZip4());
    }

    public void updateGeocache(StreetAddress nysStreetAddress, Geocode nysGeocode) {
        baseDao.tigerJbdcTemplate.update(RegeocacheQuery.UPDATE_GEOCACHE.getSql(baseDao.getCacheSchema()),
                "POINT(" + nysGeocode.getLon() + " " + nysGeocode.getLat() + ")",
                nysGeocode.getMethod(), nysGeocode.getQuality().name(), nysStreetAddress.getZip4(),
                nysStreetAddress.getBldgNum(), nysStreetAddress.getStreetName(),
                nysStreetAddress.getStreetType(), nysStreetAddress.getPreDir(),
                nysStreetAddress.getPostDir()
        );
    }

    public List<String> getAllZips() {
        return baseDao.geoApiJbdcTemplate.query(
                RegeocacheQuery.SELECT_ZIPS.getSql(), (rs, rowNum) -> rs.getString("zcta5ce10"));
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

}

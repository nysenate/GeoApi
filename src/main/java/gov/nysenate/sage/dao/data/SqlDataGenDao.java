package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.datagen.RubberBandedBoundary;
import gov.nysenate.sage.model.datagen.ManualZipCodePoint;
import gov.nysenate.sage.scripts.streetfinder.County;
import gov.nysenate.sage.scripts.streetfinder.TownCode;
import gov.nysenate.sage.model.datagen.ZipCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SqlDataGenDao implements DataGenDao {

    private BaseDao baseDao;

    @Autowired
    public SqlDataGenDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    public List<County> getCountyCodes() {
        return baseDao.geoApiJbdcTemplate.query(
                DataGenQuery.SELECT_SENATE_COUNTY_CODES.getSql(
                        baseDao.getPublicSchema()), (rs, rowNum) ->
                        new County(rs.getString("name"),rs.getString("id") ));
    }

    public List<TownCode> getTownCodes() {
        return baseDao.geoApiJbdcTemplate.query(
                DataGenQuery.SELECT_TOWN_CODES.getSql(
                        baseDao.getDistrictSchema()), (rs, rowNum) ->
                        new TownCode(rs.getString("name"),rs.getString("abbrev") ));
    }

    public List<ZipCode> getZipCodes() {
        return baseDao.geoApiNamedJbdcTemaplate.query(
                DataGenQuery.SELECT_DISTRICT_ZIP.getSql(
                        baseDao.getDistrictSchema()), (rs, rowNum) ->
                        new ZipCode(rs.getString("zcta5ce10")));
    }

    public List<RubberBandedBoundary> getAddressPointGeoJson() {
        return baseDao.tigerJbdcTemplate.query(
                DataGenQuery.SELECT_ADDRESSPOINT_AS_GEO_JSON.getSql(
                        baseDao.getGeocoderPublicSchema()), (rs, rowNum) ->
                        new RubberBandedBoundary(rs.getString("zipcode"),rs.getString("geo")));
    }

    public List<RubberBandedBoundary> getGeoCacheGeoJson() {
        return baseDao.tigerJbdcTemplate.query(
                DataGenQuery.SELECT_ADDRESSPOINT_AS_GEO_JSON.getSql(
                        baseDao.getGeocoderPublicSchema()), (rs, rowNum) ->
                        new RubberBandedBoundary(rs.getString("zip5"),rs.getString("geo")));
    }

    public void insertIntoManualDataentryGeopoints(ManualZipCodePoint superManualZipCode) {
        baseDao.geoApiJbdcTemplate.update((DataGenQuery.INSERT_MANUAL_DATAENTRY_GEOPOINT.getSql(baseDao.getPublicSchema())),superManualZipCode.getZipcode(),
                superManualZipCode.getType(),superManualZipCode.getLon(),superManualZipCode.getLat(),superManualZipCode.getSource());
    }

    public List<RubberBandedBoundary> getManualDataentryGeopoints() {
        return baseDao.geoApiJbdcTemplate.query(
                DataGenQuery.SELECT_MANUAL_DATAENTRY_GEOPOINT.getSql(
                        baseDao.getPublicSchema()), (rs, rowNum) ->
                        new RubberBandedBoundary(rs.getString("zipcode"),rs.getString("geo")));
    }



}

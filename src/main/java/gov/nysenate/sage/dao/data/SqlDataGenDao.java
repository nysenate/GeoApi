package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BaseDao;
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


}

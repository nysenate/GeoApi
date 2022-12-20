package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.scripts.streetfinder.NamePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SqlDataGenDao implements DataGenDao {

    private final BaseDao baseDao;

    @Autowired
    public SqlDataGenDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    public List<NamePair> getCountyCodes() {
        return baseDao.geoApiJbdcTemplate.query(
                DataGenQuery.SELECT_SENATE_COUNTY_CODES.getSql(
                        baseDao.getPublicSchema()), (rs, rowNum) ->
                        new NamePair(rs.getString("name"), rs.getString("id") ));
    }

    public List<NamePair> getTownCodes() {
        return baseDao.geoApiJbdcTemplate.query(
                DataGenQuery.SELECT_TOWN_CODES.getSql(
                        baseDao.getDistrictSchema()), (rs, rowNum) ->
                        new NamePair(rs.getString("name"), rs.getString("abbrev") ));
    }
}

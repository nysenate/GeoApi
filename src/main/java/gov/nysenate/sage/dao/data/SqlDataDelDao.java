package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BaseDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public class SqlDataDelDao implements DataDelDao {

    private BaseDao baseDao;

    @Autowired
    public SqlDataDelDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    //Zip Code Sql
    public Integer getGeocacheDistinctZipCodesCount() {
        List<Integer> countList =  baseDao.tigerJbdcTemplate.query(
                DataDelQuery.ZIP_COUNT_SQL.getSql(
                        baseDao.getCacheSchema()), (rs, rowNum) ->
                        rs.getInt("count"));

        if (countList.isEmpty()) {
            return null;
        } else {
            return countList.get(0);
        }
    }

    public List<String> getGeocacheZipBatch(int limit, int offset) {
        return baseDao.tigerJbdcTemplate.query(
                DataDelQuery.ZIP_BATCH_SQL.getSql(
                        baseDao.getCacheSchema()), (rs, rowNum) ->
                        rs.getString("zip5"), limit, offset);
    }

    public void deleteZipInGeocache(String zip) {
        baseDao.tigerJbdcTemplate.update(
                DataDelQuery.DELETE_ZIP_SQL.getSql(
                        baseDao.getCacheSchema()), zip);
    }


    //State sql
    public Integer getGeocacheDistinctStatesCount() {
        List<Integer> countList =  baseDao.tigerJbdcTemplate.query(
                DataDelQuery.STATE_COUNT_SQL.getSql(
                        baseDao.getCacheSchema()), (rs, rowNum) ->
                        rs.getInt("count"));

        if (countList.isEmpty()) {
            return null;
        } else {
            return countList.get(0);
        }
    }

    public List<String> getGeocacheStateBatch(int limit, int offset) {
        return baseDao.tigerJbdcTemplate.query(
                DataDelQuery.STATE_BATCH_SQL.getSql(
                        baseDao.getCacheSchema()), (rs, rowNum) ->
                        rs.getString("state"), limit, offset);
    }

    public void deleteStateInGeocache(String state) {
        baseDao.tigerJbdcTemplate.update(
                DataDelQuery.DELETE_STATE_SQL.getSql(
                        baseDao.getCacheSchema()), state);
    }
}

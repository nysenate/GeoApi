package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BaseDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class SqlPostOfficeDao implements PostOfficeDao {
    private final BaseDao baseDao;

    @Autowired
    public SqlPostOfficeDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    @Override
    public String getPostOfficeAddress(int zip) {
        String sql = PostOfficeQuery.GET_ADDRESS_FROM_ZIP.getSql(baseDao.getPublicSchema());
        var params = new MapSqlParameterSource("zip5", zip);
        try {
            return baseDao.geoApiNamedJbdcTemplate.queryForObject(sql, params, String.class);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }
}

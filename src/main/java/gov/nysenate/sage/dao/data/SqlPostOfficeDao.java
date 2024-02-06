package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.PostOfficeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class SqlPostOfficeDao implements PostOfficeDao {
    private final BaseDao baseDao;

    @Autowired
    public SqlPostOfficeDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    @Override
    public List<PostOfficeAddress> getAllPostOffices() {
        String sql = PostOfficeQuery.GET_ALL_POST_OFFICES.getSql(baseDao.getPublicSchema());
        return baseDao.geoApiNamedJbdcTemplate.query(sql, new PostOfficeHandler());
    }

    @Override
    public synchronized void replaceData(List<PostOfficeAddress> newData) {
        baseDao.geoApiNamedJbdcTemplate.update(PostOfficeQuery.CLEAR_TABLE.getSql(baseDao.getPublicSchema()), Map.of());
        for (PostOfficeAddress address : newData) {
            var params = new MapSqlParameterSource("deliveryZip", address.deliveryZip())
                    .addValue("streetWithNum", address.streetWithNum())
                    .addValue("city", address.city())
                    .addValue("zip5", address.zip5())
                    .addValue("zip4", address.zip4());
            String sql = PostOfficeQuery.ADD_ADDRESS.getSql(baseDao.getPublicSchema());
            baseDao.geoApiNamedJbdcTemplate.update(sql, params);
        }
    }

    private static class PostOfficeHandler implements RowMapper<PostOfficeAddress> {
        @Override
        public PostOfficeAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new PostOfficeAddress(rs.getInt("deliveryZip"), rs.getString("address"),
                    rs.getString("city"), rs.getInt("zip5"), rs.getInt("zip4"));
        }
    }
}

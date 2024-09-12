package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.PostOfficeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
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

    @Nonnull
    @Override
    public List<PostOfficeAddress> getPostOffices(int deliveryZip) {
        String sql = PostOfficeQuery.GET_ADDRESSES_BY_DELIVERY_ZIP.getSql(baseDao.getPublicSchema());
        var params = new MapSqlParameterSource("deliveryZip", deliveryZip);
        return baseDao.geoApiNamedJbdcTemplate.query(sql, params, new PostOfficeHandler());
    }

    @Override
    public synchronized void replaceData(List<PostOfficeAddress> postalAddresses) {
        baseDao.geoApiNamedJbdcTemplate.update(PostOfficeQuery.CLEAR_TABLE.getSql(baseDao.getPublicSchema()), Map.of());
        for (PostOfficeAddress postalAddress : postalAddresses) {
            Address address = postalAddress.address();
            var params = new MapSqlParameterSource("deliveryZip", postalAddress.deliveryZip())
                    .addValue("streetWithNum", address.getAddr1())
                    .addValue("city", address.getPostalCity())
                    .addValue("zip5", address.getZip5())
                    .addValue("zip4", address.getZip4());
            String sql = PostOfficeQuery.ADD_ADDRESS.getSql(baseDao.getPublicSchema());
            baseDao.geoApiNamedJbdcTemplate.update(sql, params);
        }
    }

    private static class PostOfficeHandler implements RowMapper<PostOfficeAddress> {
        @Override
        public PostOfficeAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
            var address = new Address(rs.getString("street_with_num"));
            address.setPostalCity(rs.getString("city"));
            address.setZip9(rs.getString("zip5") + "-" + rs.getString("zip4"));
            return new PostOfficeAddress(rs.getInt("delivery_zip"), address);
        }
    }
}

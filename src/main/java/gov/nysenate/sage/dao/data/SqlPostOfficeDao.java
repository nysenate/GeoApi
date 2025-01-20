package gov.nysenate.sage.dao.data;

import com.google.common.collect.Multimap;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.BuildingAddress;
import gov.nysenate.sage.model.address.Zip5;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class SqlPostOfficeDao extends BaseDao implements PostOfficeDao {
    @Nonnull
    @Override
    public List<BuildingAddress> getPostOffices(int deliveryZip) {
        String sql = PostOfficeQuery.GET_ADDRESSES_BY_DELIVERY_ZIP.getSql(getPublicSchema());
        var params = new MapSqlParameterSource("deliveryZip", deliveryZip);
        return geoApiNamedJbdcTemplate.query(sql, params, new PostOfficeHandler());
    }

    @Override
    public synchronized void replaceData(Multimap<Zip5, BuildingAddress> postOfficeMap) {
        geoApiNamedJbdcTemplate.update(PostOfficeQuery.CLEAR_TABLE.getSql(getPublicSchema()), Map.of());
        for (var postalAddress : postOfficeMap.entries()) {
            Address address = postalAddress.getValue();
            var params = new MapSqlParameterSource("deliveryZip", postalAddress.getKey())
                    .addValue("streetWithNum", address.getAddr1())
                    .addValue("city", address.getPostalCity())
                    .addValue("zip5", address.getZip5())
                    .addValue("zip4", address.getZip4());
            String sql = PostOfficeQuery.ADD_ADDRESS.getSql(getPublicSchema());
            geoApiNamedJbdcTemplate.update(sql, params);
        }
    }

    private static class PostOfficeHandler implements RowMapper<BuildingAddress> {
        @Override
        public BuildingAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
            var address = new BuildingAddress(rs.getString("street_with_num"), rs.getString("city"),
                    rs.getString("zip5"));
            address.setZip4(rs.getString("zip4"));
            return address;
        }
    }
}

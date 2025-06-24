package gov.nysenate.sage.dao.data;

import com.google.common.collect.Multimap;
import gov.nysenate.sage.dao.base.BaseDao;
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
    public List<BuildingAddress> getPostOffices(Zip5 deliveryZip) {
        if (deliveryZip == null) {
            return List.of();
        }
        String sql = PostOfficeQuery.GET_ADDRESSES_BY_DELIVERY_ZIP.getSql(getPublicSchema());
        var params = new MapSqlParameterSource("deliveryZip", deliveryZip.toString());
        return namedJdbcTemplate.query(sql, params, new PostOfficeHandler());
    }

    @Override
    public synchronized void replaceData(Multimap<Zip5, BuildingAddress> postOfficeMap) {
        namedJdbcTemplate.update(PostOfficeQuery.CLEAR_TABLE.getSql(getPublicSchema()), Map.of());
        for (var postalAddress : postOfficeMap.entries()) {
            BuildingAddress address = postalAddress.getValue();
            var params = new MapSqlParameterSource("deliveryZip", postalAddress.getKey().toString())
                    .addValue("bldgId", address.getBldgId())
                    .addValue("street", address.getStreet())
                    .addValue("city", address.getPostalCity())
                    .addValue("zip5", address.getZip5().toString())
                    .addValue("zip4", address.getZip4() == null ? null : address.getZip4().toString());
            String sql = PostOfficeQuery.ADD_ADDRESS.getSql(getPublicSchema());
            namedJdbcTemplate.update(sql, params);
        }
    }

    private static class PostOfficeHandler implements RowMapper<BuildingAddress> {
        @Override
        public BuildingAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new BuildingAddress(rs.getString("bldg_id"), rs.getString("street"),
                    rs.getString("city"), "NY", rs.getString("zip5"), rs.getString("zip4"));
        }
    }
}

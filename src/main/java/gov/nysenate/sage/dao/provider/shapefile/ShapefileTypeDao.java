package gov.nysenate.sage.dao.provider.shapefile;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.district.DistrictTableInfo;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static gov.nysenate.sage.dao.provider.shapefile.ShapefileQueries.GET_ALL_TYPE_INFO;
import static gov.nysenate.sage.dao.provider.shapefile.ShapefileQueries.GET_SINGLE_TYPE_INFO;

/**
 * DAO to manipulate basic information related to shapefile tables in the database.
 */
@Repository
public class ShapefileTypeDao extends BaseDao {
    private static final RowMapper<DistrictTableInfo> tableInfoRowMapper = new TableInfoRowMapper();

    public DistrictTableInfo getDistrictTypeInfo(DistrictType districtType) {
        var params = new MapSqlParameterSource("typeName", districtType.name());
        return namedJdbcTemplate.queryForObject(GET_SINGLE_TYPE_INFO.getSql("districts"), params, tableInfoRowMapper);
    }

    public List<DistrictTableInfo> getTableInfos() {
        return namedJdbcTemplate.query(GET_ALL_TYPE_INFO.getSql("districts"), new TableInfoRowMapper());
    }

    public static class TableInfoRowMapper implements RowMapper<DistrictTableInfo> {
        @Override
        public @Nullable DistrictTableInfo mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            return new DistrictTableInfo(
                    DistrictType.valueOf(rs.getString("type_name").toUpperCase()),
                    rs.getString("code_column"), rs.getString("name_column")
            );
        }
    }
}

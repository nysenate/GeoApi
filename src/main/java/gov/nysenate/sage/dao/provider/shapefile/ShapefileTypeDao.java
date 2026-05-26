package gov.nysenate.sage.dao.provider.shapefile;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.district.DistrictTypeInfo;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.EnumMap;

import static gov.nysenate.sage.dao.provider.shapefile.ShapefileQueries.GET_ALL_TYPE_INFO;
import static gov.nysenate.sage.dao.provider.shapefile.ShapefileQueries.GET_SINGLE_TYPE_INFO;

/**
 * DAO to manipulate basic information related to shapefile tables in the database.
 */
@Repository
public class ShapefileTypeDao extends BaseDao {
    private static final RowMapper<DistrictTypeInfo> typeInfoRowMapper = (rs, rowNum) ->
            new DistrictTypeInfo(rs.getString("code_column"), rs.getString("name_column"));

    public DistrictTypeInfo getDistrictTypeInfo(DistrictType districtType) {
        var params = new MapSqlParameterSource("typeName", districtType.name());
        return namedJdbcTemplate.queryForObject(GET_SINGLE_TYPE_INFO.getSql("districts"), params, typeInfoRowMapper);
    }

    public EnumMap<DistrictType, DistrictTypeInfo> getTypeInfoMap() {
        var tempMap = new EnumMap<DistrictType, DistrictTypeInfo>(DistrictType.class);
        namedJdbcTemplate.query(GET_ALL_TYPE_INFO.getSql("districts"), rs -> {
            tempMap.put(DistrictType.valueOf(rs.getString("type_name").toUpperCase()),
                    typeInfoRowMapper.mapRow(rs, 0)
            );
        });
        return tempMap;
    }
}

package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.provider.shapefile.ShapefileTypeDao;
import gov.nysenate.sage.dao.provider.streetfile.StreetfileDao;
import gov.nysenate.sage.model.district.DistrictId;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictTableInfo;
import gov.nysenate.sage.model.district.DistrictType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class DistrictInfoDao extends BaseDao {
    private static final String NAME_COLUMN = "name";
    private final ShapefileTypeDao typeDao;

    @Autowired
    public DistrictInfoDao(ShapefileTypeDao typeDao) {
        this.typeDao = typeDao;
    }

    public Map<DistrictId, DistrictInfo> getInfoMap(DistrictType type) {
        DistrictTableInfo tableInfo = typeDao.getDistrictTypeInfo(type);
        if (tableInfo != null) {
            return getShapefileInfoMap(tableInfo);
        }
        String codeColumn = StreetfileDao.distColMap.get(type);
        if (codeColumn == null) {
            return null;
        }

        String sql = DistrictInfoQuery.GET_STREETFILE_CODES.getSql("public", Map.of("codeColumn", codeColumn));
        var map = new HashMap<DistrictId, DistrictInfo>();
        jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("code"))
                .forEach(code -> {
                    var info = new DistrictInfo();
                    info.put("name", defaultName(type, null, code));
                    map.put(new DistrictId(code), info);
                });
        return map;
    }

    /**
     * Stores every column of a district table, except the geometry, in a {@link DistrictInfo}.
     * The "name" key always exists, even for tables without a name column.
     */
    private Map<DistrictId, DistrictInfo> getShapefileInfoMap(DistrictTableInfo tableInfo) {
        String sql = DistrictInfoQuery.GET_SHAPEFILE_DATA.getSql("districts", tableInfo.getReplacements("type"));
        var map = new HashMap<DistrictId, DistrictInfo>();
        for (Map<String, Object> row : jdbcTemplate.queryForList(sql)) {
            row.remove("gid");
            row.remove("geom");
            var info = new DistrictInfo();
            row.forEach((column, value) -> info.put(column, Objects.toString(value, null)));
            var id = new DistrictId(row.get(tableInfo.idColumn()).toString());
            // A table without a name column would leave the name null.
            String name = Objects.toString(row.get(NAME_COLUMN), null);
            info.put(NAME_COLUMN, switch (tableInfo.type()) {
                case COUNTY -> name + " County";
                case VILLAGE -> "Village of " + name;
                default -> defaultName(tableInfo.type(), name, id.id());
            });
            map.put(id, info);
        }
        return map;
    }

    private static String defaultName(DistrictType type, String name, String idOrCode) {
        if (name != null) {
            return name;
        }
        return switch (type) {
            case ZIP -> "Zipcode " + idOrCode;
            case WARD -> "Ward " + idOrCode;
            default -> type.getDisplayName() + " District " + idOrCode;
        };
    }
}

package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.model.townCity.TownCityDao;
import gov.nysenate.sage.dao.provider.shapefile.ShapefileTypeDao;
import gov.nysenate.sage.dao.provider.streetfile.StreetfileDao;
import gov.nysenate.sage.model.district.DistrictTableInfo;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.district.TownCity;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DistrictNameDao extends BaseDao {
    private final ShapefileTypeDao typeDao;
    private final TownCityDao townCityDao;

    @Autowired
    public DistrictNameDao(ShapefileTypeDao typeDao, TownCityDao townCityDao) {
        this.typeDao = typeDao;
        this.townCityDao = townCityDao;
    }

    public Map<String, String> getNameMap(DistrictType type) {
        DistrictTableInfo tableInfo = typeDao.getDistrictTypeInfo(type);
        if (tableInfo != null) {
            return jdbcTemplate.query(NameQuery.GET_SHAPEFILE_DATA.getSql("districts",
                    tableInfo.getReplacements("type")), new NameMapExtractor(tableInfo));
        }
        String codeColumn = StreetfileDao.distColMap.get(type);
        if (codeColumn == null) {
            return null;
        }

        String sql = NameQuery.GET_STREETFILE_CODES.getSql("public", Map.of("codeColumn", codeColumn));
        var map = new HashMap<String, String>();
        jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("code"))
                .forEach(code -> map.put(code, defaultName(type, null, code)));
        return map;
    }

    private class NameMapExtractor implements ResultSetExtractor<Map<String, String>> {
        private final DistrictTableInfo tableInfo;

        private NameMapExtractor(DistrictTableInfo tableInfo) {
            this.tableInfo = tableInfo;
        }

        @Override
        public Map<String, String> extractData(@NonNull ResultSet rs)
                throws SQLException, DataAccessException {
            var map = new HashMap<String, String>();
            Map<String, String> townCityCodeToNameMap = null;
            if (tableInfo.type() == DistrictType.TOWN_CITY) {
                townCityCodeToNameMap = townCityDao.getTownCities().stream().collect(
                        Collectors.toMap(TownCity::code, TownCity::fullName)
                );
            }
            while (rs.next()) {
                String code = rs.getString("code");
                String name = rs.getString("name");
                name = switch (tableInfo.type()) {
                    case COUNTY -> name + " County";
                    case TOWN_CITY -> townCityCodeToNameMap.get(code);
                    case VILLAGE -> name + " Village";
                    default -> defaultName(tableInfo.type(), name, code);
                };
                map.put(code, name);
            }
            return map;
        }
    }

    private static String defaultName(DistrictType type, String name, String code) {
        if (name != null) {
            return name;
        }
        return switch (type) {
            case ZIP -> "Zipcode " + code;
            case WARD -> "Ward " + code;
            default -> type.getDisplayName() + " District " + code;
        };
    }
}

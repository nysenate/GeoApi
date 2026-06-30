package gov.nysenate.sage.dao.model.townCity;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.district.DistrictTableInfo;
import gov.nysenate.sage.model.district.TownCity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class TownCityDao extends BaseDao {
    public Set<TownCity> getTownCities(DistrictTableInfo townCityInfo) {
        return new HashSet<>(namedJdbcTemplate.query(TownCityQuery.SELECT_ALL.getSql(),
                new TownCityRowMapper(townCityInfo))
        );
    }

    private class TownCityRowMapper implements RowMapper<TownCity> {
        private final DistrictTableInfo townCityInfo;
        private final List<String> repeatNames = namedJdbcTemplate.query(
                TownCityQuery.SELECT_ALL_REPEAT_NAMES.getSql(), (rs, rowNum) -> rs.getString("name")
        );

        private TownCityRowMapper(DistrictTableInfo townCityInfo) {
            this.townCityInfo = townCityInfo;
        }

        @Override
        public TownCity mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            String code = rs.getString(townCityInfo.codeColumn());
            String baseName = rs.getString(townCityInfo.nameColumn());
            String voterFileCode = namedJdbcTemplate.queryForObject(TownCityQuery.SELECT_VOTER_FILE_CODE.getSql(),
                    Map.of("code", code), new SingleColumnRowMapper<>());
            return new TownCity(baseName, rs.getString("muni_type"),
                    repeatNames.contains(baseName), code, voterFileCode,
                    rs.getString("county"));
        }
    }
}

package gov.nysenate.sage.dao.model.townCity;

import com.google.common.collect.ImmutableMap;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.provider.DistrictNameDao;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.district.TownCity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class TownCityDao extends BaseDao {
    private final DistrictNameDao nameDao;
    private ImmutableMap<String, TownCity> codeToTownCityMap;

    @Autowired
    public TownCityDao(DistrictNameDao nameDao) {
        this.nameDao = nameDao;
    }

    @PostConstruct
    public void cacheTownCities() {
        ImmutableMap.Builder<String, TownCity> builder = ImmutableMap.builder();
        jdbcTemplate.query(TownCityQuery.SELECT_ALL.getSql(), new NestedMapCallbackHandler())
                .forEach(townCity -> builder.put(townCity.code(), townCity));
        this.codeToTownCityMap = builder.build();
    }

    public TownCity getTownCityByCode(String code) {
        return codeToTownCityMap.get(code);
    }

    private class NestedMapCallbackHandler implements RowMapper<TownCity> {
        @Override
        public TownCity mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            String code = rs.getString("district_code");
            String fullName = nameDao.getDistrictName(DistrictType.TOWN_CITY, code);
            return new TownCity(fullName, code, rs.getString("voterfile_code"));
        }
    }
}

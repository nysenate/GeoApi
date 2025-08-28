package gov.nysenate.sage.dao.model.townCity;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.provider.DistrictNameDao;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.district.TownCity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Repository
public class TownCityDao extends BaseDao {
    private final DistrictNameDao nameDao;

    @Autowired
    public TownCityDao(DistrictNameDao nameDao) {
        this.nameDao = nameDao;
    }

    public Set<TownCity> townCities() {
        var rch = new NestedMapCallbackHandler();
        jdbcTemplate.query(TownCityQuery.SELECT_ALL.getSql(), rch);
        return rch.results;
    }

    private class NestedMapCallbackHandler implements RowCallbackHandler {
        private final Set<TownCity> results = new HashSet<>();

        @Override
        public void processRow(@Nonnull ResultSet rs) throws SQLException {
            String code = rs.getString("district_code");
            String fullName = nameDao.getDistrictName(DistrictType.TOWN_CITY, code);
            results.add(new TownCity(fullName, code, rs.getString("voterfile_code")));
        }
    }
}

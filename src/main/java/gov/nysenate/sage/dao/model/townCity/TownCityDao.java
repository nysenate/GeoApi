package gov.nysenate.sage.dao.model.townCity;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.provider.district.MunicipalityType;
import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static gov.nysenate.sage.dao.provider.district.MunicipalityType.CITY;
import static gov.nysenate.sage.dao.provider.district.MunicipalityType.TOWN;

@Repository
public class TownCityDao {
    private final BaseDao baseDao;

    @Autowired
    public TownCityDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    public Map<String, String> getAbbrevToNameMap() {
        var rch = new SimpleMapCallbackHandler();
        baseDao.geoApiNamedJbdcTemplate.query(TownCityQuery.SELECT_ALL.getSql(), Map.of(), rch);
        return rch.abbrevToNameMap;
    }

    public Map<MunicipalityType, Map<String, String>> getTypeAndNameToAbbrevMap() {
        var rch = new NestedMapCallbackHandler();
        baseDao.geoApiJbdcTemplate.query(TownCityQuery.SELECT_ALL.getSql(), rch);
        return rch.results;
    }

    private static final class SimpleMapCallbackHandler implements RowCallbackHandler {
        private final Map<String, String> abbrevToNameMap = new HashMap<>();

        @Override
        public void processRow(@Nonnull ResultSet rs) throws SQLException {
            while (rs.next()) {
                abbrevToNameMap.put(rs.getString("abbrev"), rs.getString("name"));
            }
        }
    }

    private static class NestedMapCallbackHandler implements RowCallbackHandler {
        private final Map<MunicipalityType, Map<String, String>> results =
                Map.of(TOWN, new CaseInsensitiveKeyMap<>(), CITY, new CaseInsensitiveKeyMap<>());

        @Override
        public void processRow(@Nonnull ResultSet rs) throws SQLException {
            String name = rs.getString("name");
            String abbrev = rs.getString("abbrev");
            Map<String, String> currMap = results.get(rs.getInt("ct_type") == 2 ? TOWN : CITY);
            currMap.put(name, abbrev);
            currMap.put(name.replaceAll(" ", ""), abbrev);
        }
    }
}

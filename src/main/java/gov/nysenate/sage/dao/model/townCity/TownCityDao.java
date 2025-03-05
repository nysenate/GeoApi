package gov.nysenate.sage.dao.model.townCity;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.provider.district.MunicipalityType;
import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static gov.nysenate.sage.dao.provider.district.MunicipalityType.CITY;
import static gov.nysenate.sage.dao.provider.district.MunicipalityType.TOWN;

@Repository
public class TownCityDao extends BaseDao {
    public Map<MunicipalityType, Map<String, String>> getTypeAndNameToAbbrevMap() {
        var rch = new NestedMapCallbackHandler();
        jdbcTemplate.query(TownCityQuery.SELECT_ALL.getSql(), rch);
        return rch.results;
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

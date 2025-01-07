package gov.nysenate.sage.dao.model.townCity;

import gov.nysenate.sage.dao.base.BaseDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Repository
public class TownCityDao {
    private final BaseDao baseDao;

    @Autowired
    public TownCityDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    public Map<String, String> getAbbrevToNameMap() {
        var rch = new MapCallbackHandler();
        baseDao.geoApiNamedJbdcTemplate.query(TownCityQuery.SELECT_ALL.getSql(), Map.of(), rch);
        return rch.abbrevToNameMap;
    }

    private static final class MapCallbackHandler implements RowCallbackHandler {
        private final Map<String, String> abbrevToNameMap = new HashMap<>();

        @Override
        public void processRow(@Nonnull ResultSet rs) throws SQLException {
            while (rs.next()) {
                abbrevToNameMap.put(rs.getString("abbrev"), rs.getString("name"));
            }
        }
    }
}

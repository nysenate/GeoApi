package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.dao.base.BaseDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static gov.nysenate.sage.dao.data.DataGenQuery.SELECT_SENATE_COUNTY_CODES;
import static gov.nysenate.sage.dao.data.DataGenQuery.SELECT_TOWN_CODES;

@Repository
public class SqlDataGenDao implements DataGenDao {
    private final BaseDao baseDao;

    @Autowired
    public SqlDataGenDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    public Map<String, String> getCountyToSenateCodeMap() {
        var handler = new CodeHandler("id");
        baseDao.geoApiJbdcTemplate.query(SELECT_SENATE_COUNTY_CODES.getSql(baseDao.getPublicSchema()), handler);
        return handler.map;
    }

    public Map<String, String> getTownToAbbrevMap() {
        var handler = new CodeHandler("abbrev");
        baseDao.geoApiJbdcTemplate.query(SELECT_TOWN_CODES.getSql(baseDao.getDistrictSchema()), handler);
        return handler.map;
    }

    private static class CodeHandler implements RowCallbackHandler {
        private final Map<String, String> map = new HashMap<>();
        private final String valueColumn;

        public CodeHandler(String valueColumn) {
            this.valueColumn = valueColumn;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            map.put(rs.getString("name"), rs.getString(valueColumn));
        }
    }
}

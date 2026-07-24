package gov.nysenate.sage.dao.model.county;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.provider.shapefile.ShapefileTypeDao;
import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.model.district.DistrictTableInfo;
import gov.nysenate.sage.model.district.DistrictType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Repository
public class CountyDao extends BaseDao {
    private final ShapefileTypeDao typeDao;

    public CountyDao(ShapefileTypeDao typeDao) {
        this.typeDao = typeDao;
    }

    public Set<County> getCounties() {
        DistrictTableInfo countyInfo = typeDao.getDistrictTypeInfo(DistrictType.COUNTY);
        String sql = CountyQuery.GET_ALL_COUNTIES.getSql(
                getPublicSchema(), countyInfo.getReplacements("type"));
        return new HashSet<>(namedJdbcTemplate.query(sql, new CountyHandler()));
    }

    private static class CountyHandler implements RowMapper<County> {
        @Override
        public County mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new County(rs.getInt("code"), rs.getString("name"), rs.getString("link"));
        }
    }
}

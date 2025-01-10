package gov.nysenate.sage.dao.model.assembly;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.district.Assembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SqlAssemblyDao extends BaseDao implements AssemblyDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlAssemblyDao.class);

    /** {@inheritDoc} */
    public List<Assembly> getAssemblies() {
        try {
            return geoApiNamedJbdcTemplate.query(
                    AssemblyQuery.GET_ALL_ASSEMBLY_MEMBERS.getSql(getPublicSchema()), new AssemblyHandler());
        }
        catch (Exception ex){
            logger.error("Failed to retrieve assemblies", ex);
        }
        return null;
    }

    /** {@inheritDoc} */
    public Assembly getAssemblyByDistrict(int district) {
        try {
            var params = new MapSqlParameterSource("district", district);
            List<Assembly> assemblyList = geoApiNamedJbdcTemplate.query(
                    AssemblyQuery.GET_ASSMEBLY_MEMBER_BY_DISTRICT.getSql(getPublicSchema()),
                    params, new AssemblyHandler());

            if (assemblyList.isEmpty()) {
                return null;
            }
            return assemblyList.get(0);

        }
        catch (Exception ex) {
            logger.error("Failed to retrieve assembly", ex);
        }
        return null;
    }

    /** {@inheritDoc} */
    public void insertAssembly(Assembly assembly) {
        try {
            var params = new MapSqlParameterSource("district", assembly.getDistrict())
                    .addValue("memberName", assembly.getMemberName())
                    .addValue("memberUrl", assembly.getMemberUrl());

            int numRows = geoApiNamedJbdcTemplate.update(
                    AssemblyQuery.INSERT_ASSEMBLY_MEMBER.getSql(getPublicSchema()), params);
            if (numRows > 0) { logger.info("Added Assembly member " + assembly.getMemberName()); }
        }
        catch (Exception ex){
            logger.error("Failed to insert Assembly member", ex);
        }
    }

    /** {@inheritDoc} */
    public void deleteAssemblies(int district) {
        try {
            var params = new MapSqlParameterSource("district", district);

            geoApiNamedJbdcTemplate.update(
                    AssemblyQuery.DELETE_ASSEMBLY_DISTRICT.getSql(getPublicSchema()), params);
        }
        catch (Exception ex) {
            logger.error("Failed to delete assembly {} {}", district, ex.getMessage());
        }
    }


    private static class AssemblyHandler implements RowMapper<Assembly> {
        @Override
        public Assembly mapRow(ResultSet rs, int rowNum) throws SQLException {
            var assembly = new Assembly();
            assembly.setDistrict(rs.getInt("district"));
            assembly.setMemberName(rs.getString("membername"));
            assembly.setMemberUrl(rs.getString("memberurl"));
            return assembly;
        }
    }
}

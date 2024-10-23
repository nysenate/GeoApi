package gov.nysenate.sage.dao.model.assembly;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.district.Assembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SqlAssemblyDao implements AssemblyDao
{
    private Logger logger = LoggerFactory.getLogger(SqlAssemblyDao.class);
    private BaseDao baseDao;

    @Autowired
    public SqlAssemblyDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    /** {@inheritDoc} */
    public List<Assembly> getAssemblies()
    {
        try {
            return baseDao.geoApiNamedJbdcTemplate.query(
                    AssemblyQuery.GET_ALL_ASSEMBLY_MEMBERS.getSql(baseDao.getPublicSchema()), new AssemblyHandler());
        }
        catch (Exception ex){
            logger.error("Failed to retrieve assemblies", ex);
        }
        return null;
    }

    /** {@inheritDoc} */
    public Assembly getAssemblyByDistrict(int district)
    {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("district", district);

            List<Assembly> assemblyList = baseDao.geoApiNamedJbdcTemplate.query(
                    AssemblyQuery.GET_ASSMEBLY_MEMBER_BY_DISTRICT.getSql(baseDao.getPublicSchema()),
                    params, new AssemblyHandler());
            if (assemblyList.isEmpty()) {
                return null;
            }
            return assemblyList.get(0);

        }
        catch (Exception ex){
            logger.error("Failed to retrieve assembly", ex);
        }
        return null;
    }

    /** {@inheritDoc} */
    public void insertAssembly(Assembly assembly)
    {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("district",assembly.getDistrict());
            params.addValue("memberName", assembly.getMemberName());
            params.addValue("memberUrl",assembly.getMemberUrl());

            int numRows = baseDao.geoApiNamedJbdcTemplate.update(
                    AssemblyQuery.INSERT_ASSEMBLY_MEMBER.getSql(baseDao.getPublicSchema()), params);
            if (numRows > 0) { logger.info("Added Assembly member " + assembly.getMemberName()); }
        }
        catch (Exception ex){
            logger.error("Failed to insert Assembly member", ex);
        }
    }

    /** {@inheritDoc} */
    public void deleteAssemblies(int district)
    {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("district", district);

            baseDao.geoApiNamedJbdcTemplate.update(
                    AssemblyQuery.DELETE_ASSEMBLY_DISTRICT.getSql(baseDao.getPublicSchema()), params);
        }
        catch (Exception ex) {
            logger.error("Failed to delete assembly " + district + " " + ex.getMessage());
        }
    }


    private static class AssemblyHandler implements RowMapper<Assembly> {
        @Override
        public Assembly mapRow(ResultSet rs, int rowNum) throws SQLException {
            Assembly assembly = new Assembly();
            assembly.setDistrict(rs.getInt("district"));
            assembly.setMemberName(rs.getString("membername"));
            assembly.setMemberUrl(rs.getString("memberurl"));
            return assembly;
        }
    }
}

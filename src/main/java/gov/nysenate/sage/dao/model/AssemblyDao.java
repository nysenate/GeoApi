package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.district.Assembly;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.List;

@Repository
public class AssemblyDao
{
    private Logger logger = LoggerFactory.getLogger(AssemblyDao.class);
    private QueryRunner run;
    private BaseDao baseDao;

    @Autowired
    public AssemblyDao(BaseDao baseDao) {
        this.baseDao = baseDao;
        run = this.baseDao.getQueryRunner();
    }


    public List<Assembly> getAssemblies()
    {
        String sql = "SELECT * FROM assembly";
        BeanListHandler<Assembly> assemblyListHandler = new BeanListHandler<>(Assembly.class);
        try {
            List<Assembly> assemblies = run.query(sql, assemblyListHandler);
            return assemblies;
        }
        catch (SQLException ex){
            logger.error("Failed to retrieve assemblies", ex);
        }
        return null;
    }

    public Assembly getAssemblyByDistrict(int district)
    {
        String sql = "SELECT * FROM assembly WHERE district = ?";
        BeanHandler<Assembly> assemblyHandler = new BeanHandler<>(Assembly.class);
        try {
            Assembly assembly = run.query(sql, assemblyHandler, district);
            return assembly;
        }
        catch (SQLException ex){
            logger.error("Failed to retrieve assembly", ex);
        }
        return null;
    }

    public void insertAssembly(Assembly assembly)
    {
        String sql = "INSERT INTO assembly (district, memberName, memberUrl) VALUES (?,?,?)";
        try {
            int numRows = run.update(sql, assembly.getDistrict(), assembly.getMemberName(), assembly.getMemberUrl());
            if (numRows > 0) { logger.info("Added Assembly member " + assembly.getMemberName()); }
        }
        catch (SQLException ex){
            logger.error("Failed to insert Assembly member", ex);
        }
    }

    /**
     * Clears the assembly table.
     */
    public void deleteAssemblies()
    {
        String sql = "DELETE FROM assembly";
        try {
            run.update(sql);
        }
        catch (SQLException ex) {
            logger.error("Failed to delete assemblies " + ex.getMessage());
        }
    }

    /**
     * Removes an assembly by district.
     */
    public void deleteAssemblies(int district)
    {
        String sql = "DELETE FROM assembly WHERE district = ?";
        try {
            run.update(sql, district);
        }
        catch (SQLException ex) {
            logger.error("Failed to delete assembly " + district + " " + ex.getMessage());
        }
    }
}

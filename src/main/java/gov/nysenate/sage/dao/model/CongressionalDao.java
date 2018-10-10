package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.district.Congressional;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class CongressionalDao extends BaseDao
{
    private Logger logger = LogManager.getLogger(CongressionalDao.class);
    private QueryRunner run = getQueryRunner();

    public List<Congressional> getCongressionals()
    {
        String sql = "SELECT * FROM congressional";
        BeanListHandler<Congressional> congressionalListHandler = new BeanListHandler<>(Congressional.class);
        try {
            List<Congressional> congressionals = run.query(sql, congressionalListHandler);
            return congressionals;
        }
        catch (SQLException ex){
            logger.error("Failed to retrieve congressionals", ex);
        }
        return null;
    }

    public Congressional getCongressionalByDistrict(int district)
    {
        String sql = "SELECT * FROM congressional WHERE district = ?";
        BeanHandler<Congressional> congressionalHandler = new BeanHandler<>(Congressional.class);
        try {
            Congressional congressional = run.query(sql, congressionalHandler, district);
            return congressional;
        }
        catch (SQLException ex){
            logger.error("Failed to retrieve congressional", ex);
        }
        return null;
    }

    public void insertCongressional(Congressional congressional)
    {
        String sql = "INSERT INTO congressional (district, memberName, memberUrl) VALUES (?,?,?)";
        try {
            int numRows = run.update(sql, congressional.getDistrict(), congressional.getMemberName(), congressional.getMemberUrl());
            if (numRows > 0) { logger.info("Added Congressional member " + congressional.getMemberName()); }
        }
        catch (SQLException ex){
            logger.error("Failed to insert Congressional member", ex);
        }
    }

    /**
     * Clears the congressional table.
     */
    public void deleteCongressionals()
    {
        String sql = "DELETE FROM congressional";
        try {
            run.update(sql);
        }
        catch (SQLException ex) {
            logger.error("Failed to delete congressionals " + ex.getMessage());
        }
    }

    /**
     * Removes a congressional by district.
     */
    public void deleteCongressional(int district)
    {
        String sql = "DELETE FROM congressional WHERE district = ?";
        try {
            run.update(sql, district);
        }
        catch (SQLException ex) {
            logger.error("Failed to delete congressional " + district + ": " + ex.getMessage());
        }
    }
}
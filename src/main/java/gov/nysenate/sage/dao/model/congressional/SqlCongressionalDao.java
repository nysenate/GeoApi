package gov.nysenate.sage.dao.model.congressional;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.model.assembly.AssemblyQuery;
import gov.nysenate.sage.model.district.Congressional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SqlCongressionalDao implements CongressionalDao
{
    private Logger logger = LoggerFactory.getLogger(SqlCongressionalDao.class);
    private BaseDao baseDao;

    @Autowired
    public SqlCongressionalDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    /** {@inheritDoc} */
    public List<Congressional> getCongressionals()
    {
        try {
            return baseDao.geoApiNamedJbdcTemaplate.query(
                    CongressionalQuery.GET_ALL_CONGRESSIONAL_MEMBERS
                            .getSql(baseDao.getPublicSchema()), new CongressionalHandler());
        }
        catch (Exception ex){
            logger.error("Failed to retrieve congressionals", ex);
        }
        return null;
    }

    /** {@inheritDoc} */
    public Congressional getCongressionalByDistrict(int district)
    {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("district", district);

            List<Congressional> congressionalList = baseDao.geoApiNamedJbdcTemaplate.query(
                    CongressionalQuery.GET_CONGRESSIONAL_MEMBER_BY_DISTRICT.getSql(baseDao.getPublicSchema()),
                    params, new CongressionalHandler());

            if (congressionalList == null || congressionalList.size() == 0) {
                return null;
            }
            return congressionalList.get(0);

        }
        catch (Exception ex){
            logger.error("Failed to retrieve congressional", ex);
        }
        return null;
    }

    /** {@inheritDoc} */
    public void insertCongressional(Congressional congressional)
    {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("district",congressional.getDistrict());
            params.addValue("memberName", congressional.getMemberName());
            params.addValue("memberUrl",congressional.getMemberUrl());

            int numRows = baseDao.geoApiNamedJbdcTemaplate.update(
                    CongressionalQuery.INSERT_CONGRESSIONAL_MEMBER.getSql(baseDao.getPublicSchema()), params);
            if (numRows > 0) { logger.info("Added Congressional member " + congressional.getMemberName()); }
        }
        catch (Exception ex){
            logger.error("Failed to insert Congressional member", ex);
        }
    }

    /** {@inheritDoc} */
    public void deleteCongressionals()
    {
        try {
            baseDao.geoApiJbdcTemplate.update(CongressionalQuery.CLEAR_CONGRESS.getSql(baseDao.getPublicSchema()));
        }
        catch (Exception ex) {
            logger.error("Failed to delete congressionals " + ex.getMessage());
        }
    }

    /** {@inheritDoc} */
    public void deleteCongressional(int district)
    {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("district", district);

            baseDao.geoApiNamedJbdcTemaplate.update(
                    CongressionalQuery.DELETE_CONGRESSIONAL_DISTRICT.getSql(baseDao.getPublicSchema()), params);
        }
        catch (Exception ex) {
            logger.error("Failed to delete congressional " + district + ": " + ex.getMessage());
        }
    }

    private static class CongressionalHandler implements RowMapper<Congressional> {
        @Override
        public Congressional mapRow(ResultSet rs, int rowNum) throws SQLException {
            Congressional congressional = new Congressional();
            congressional.setDistrict(rs.getInt("district"));
            congressional.setMemberName(rs.getString("membername"));
            congressional.setMemberUrl(rs.getString("memberurl"));
            return congressional;
        }
    }
}
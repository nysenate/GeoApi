package gov.nysenate.sage.dao.model.congressional;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.district.Congressional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SqlCongressionalDao extends BaseDao implements CongressionalDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlCongressionalDao.class);

    /** {@inheritDoc} */
    public List<Congressional> getCongressionals() {
        try {
            return geoApiNamedJbdcTemplate.query(
                    CongressionalQuery.GET_ALL_CONGRESSIONAL_MEMBERS
                            .getSql(getPublicSchema()), new CongressionalHandler());
        }
        catch (Exception ex){
            logger.error("Failed to retrieve congressionals", ex);
        }
        return null;
    }

    /** {@inheritDoc} */
    public Congressional getCongressionalByDistrict(int district) {
        try {
            var params = new MapSqlParameterSource("district", district);
            List<Congressional> congressionalList = geoApiNamedJbdcTemplate.query(
                    CongressionalQuery.GET_CONGRESSIONAL_MEMBER_BY_DISTRICT.getSql(getPublicSchema()),
                    params, new CongressionalHandler());

            if (congressionalList.isEmpty()) {
                return null;
            }
            return congressionalList.get(0);

        }
        catch (Exception ex) {
            logger.error("Failed to retrieve congressional", ex);
        }
        return null;
    }

    /** {@inheritDoc} */
    public void insertCongressional(Congressional congressional) {
        try {
            var params = new MapSqlParameterSource("district", congressional.getDistrict())
                    .addValue("memberName", congressional.getMemberName())
                    .addValue("memberUrl", congressional.getMemberUrl());

            int numRows = geoApiNamedJbdcTemplate.update(
                    CongressionalQuery.INSERT_CONGRESSIONAL_MEMBER.getSql(getPublicSchema()), params);
            if (numRows > 0) {
                logger.info("Added Congressional member {}", congressional.getMemberName());
            }
        }
        catch (Exception ex) {
            logger.error("Failed to insert Congressional member", ex);
        }
    }

    /** {@inheritDoc} */
    public void deleteCongressional(int district) {
        try {
            var params = new MapSqlParameterSource("district", district);
            geoApiNamedJbdcTemplate.update(
                    CongressionalQuery.DELETE_CONGRESSIONAL_DISTRICT.getSql(getPublicSchema()), params);
        }
        catch (Exception ex) {
            logger.error("Failed to delete congressional {}: {}", district, ex.getMessage());
        }
    }

    private static class CongressionalHandler implements RowMapper<Congressional> {
        @Override
        public Congressional mapRow(ResultSet rs, int rowNum) throws SQLException {
            var congressional = new Congressional();
            congressional.setDistrict(rs.getInt("district"));
            congressional.setMemberName(rs.getString("membername"));
            congressional.setMemberUrl(rs.getString("memberurl"));
            return congressional;
        }
    }
}
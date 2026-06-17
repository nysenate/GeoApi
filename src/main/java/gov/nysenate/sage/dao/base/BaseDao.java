package gov.nysenate.sage.dao.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public abstract class BaseDao {
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    protected NamedParameterJdbcTemplate namedJdbcTemplate;

    protected String getJobSchema() {
        return "job";
    }

    protected String getPublicSchema() {
        return "public";
    }

    protected String getLogSchema() {
        return "log";
    }
}

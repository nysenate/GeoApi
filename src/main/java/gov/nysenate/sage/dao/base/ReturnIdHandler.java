package gov.nysenate.sage.dao.base;

import org.apache.commons.dbutils.ResultSetHandler;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class ReturnIdHandler implements ResultSetHandler<Integer>
{
    @Override
    public Integer handle(ResultSet rs) throws SQLException {
        return (rs.next()) ? rs.getInt("id") : 0;
    }
}

package gov.nysenate.sage.dao.logger.address;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.Address;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SqlAddressLogger implements AddressLogger
{
    private static Logger logger = LoggerFactory.getLogger(SqlAddressLogger.class);
    private BaseDao baseDao;


    @Autowired
    public SqlAddressLogger(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    /** {@inheritDoc} */
    public int logAddress(Address address)
    {
        if (address != null) {
            try {
                int retrievedId = getAddressId(address);
                if (retrievedId > 0) return retrievedId;

                MapSqlParameterSource params = getAddrParams(address);
                List<Integer> idList = baseDao.geoApiNamedJbdcTemaplate.query(AddressQuery.INSERT_ADDRESS.getSql(baseDao.getLogSchema()), params, new AddressIdHandler());
                if (idList == null || idList.size() <= 0) {
                    return 0;
                }
                return idList.get(0);
            }
            catch (Exception ex) {
                logger.error("Failed to log address!", ex);
            }
        }
        return 0;
    }

    /** {@inheritDoc} */
    public int getAddressId(Address address)
    {
        if (address != null) {
            try {

                MapSqlParameterSource params = getAddrParams(address);
                List<Integer> idList = baseDao.geoApiNamedJbdcTemaplate.query(AddressQuery.GET_ADDRESS_ID.getSql(baseDao.getLogSchema()), params, new AddressIdHandler());
                if (idList == null || idList.size() <= 0) {
                    return 0;
                }
                return idList.get(0);
            }
            catch (Exception ex) {
                logger.error("Failed to get address id!", ex);
            }
        }
        return 0;
    }

    private MapSqlParameterSource getAddrParams(Address address) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("addr1",address.getAddr1());
        params.addValue("addr2", address.getAddr2());
        params.addValue("city", address.getCity());
        params.addValue("state",address.getState());
        params.addValue("zip5", address.getZip5());
        params.addValue("zip4",address.getZip4());
        return params;
    }

    private static class AddressIdHandler implements RowMapper<Integer> {
        @Override
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt("id");
        }
    }
}

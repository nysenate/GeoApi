package gov.nysenate.sage.provider.cityzip;

import gov.nysenate.sage.dao.provider.cityzip.SqlCityZipDBDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Service
public class CityZipDB implements CityZipService
{
    private static Logger logger = LoggerFactory.getLogger(CityZipDB.class);
    private SqlCityZipDBDao sqlCityZipDBDao;

    @Autowired
    public CityZipDB(SqlCityZipDBDao sqlCityZipDBDao) {
        this.sqlCityZipDBDao = sqlCityZipDBDao;
    }

    /** {@inheritDoc} */
    @Override
    public String getCityByZip(String zip5)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getZipsByCity(String city)
    {
        if (city != null && !city.isEmpty()) {
            return sqlCityZipDBDao.getZipsByCity(city);
        }
        return new ArrayList<>();
    }
}

package gov.nysenate.sage.provider.cityzip;

import gov.nysenate.sage.dao.provider.cityzip.SqlCityZipDBDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityZipDB implements CityZipService {
    private static Logger logger = LoggerFactory.getLogger(CityZipDB.class);
    private final SqlCityZipDBDao sqlCityZipDBDao;

    @Autowired
    public CityZipDB(SqlCityZipDBDao sqlCityZipDBDao) {
        this.sqlCityZipDBDao = sqlCityZipDBDao;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        CityZipDB.logger = logger;
    }

    /** {@inheritDoc} */
    @Override
    public List<Integer> getZipsByCity(String city) {
        if (city == null || city.isEmpty()) {
            return List.of();
        }
        return sqlCityZipDBDao.getZipsByCity(city);
    }
}

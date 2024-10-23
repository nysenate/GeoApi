package gov.nysenate.sage.provider.cityzip;

import gov.nysenate.sage.dao.provider.cityzip.SqlCityZipDBDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityZipDB implements CityZipService {
    private final SqlCityZipDBDao sqlCityZipDBDao;

    @Autowired
    public CityZipDB(SqlCityZipDBDao sqlCityZipDBDao) {
        this.sqlCityZipDBDao = sqlCityZipDBDao;
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

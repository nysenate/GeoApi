package gov.nysenate.sage.provider;

import com.google.common.collect.Lists;
import gov.nysenate.sage.dao.provider.CityZipDBDao;
import gov.nysenate.sage.service.address.CityZipService;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CityZipDB implements CityZipService
{
    private static Logger logger = Logger.getLogger(CityZipDB.class);
    private static CityZipDBDao cityZipDBDao = new CityZipDBDao();

    @Override
    public String getCityByZip(String zip5)
    {
        return null;
    }

    /**
     * Retrieves a list of zip5 codes that are contained within the given city.
     * @param city Name of the city
     * @return List of zip5 codes
     */
    @Override
    public List<String> getZipsByCity(String city)
    {
        if (city != null && !city.isEmpty()) {
            return cityZipDBDao.getZipsByCity(city);
        }
        return new ArrayList<>();
    }
}

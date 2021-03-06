package gov.nysenate.sage.dao.provider.cityzip;

import java.util.List;

public interface CityZipDBDao {

    /**
     * Returns a list of zip codes given a city name.
     * @param city
     * @return List of matching zip5 strings.
     */
    public List<String> getZipsByCity(String city);
}

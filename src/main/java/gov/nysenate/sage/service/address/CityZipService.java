package gov.nysenate.sage.service.address;

import java.util.List;

public interface CityZipService
{
    public String getCityByZip(String zip5);
    public List<String> getZipsByCity(String city);
}

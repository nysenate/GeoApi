package gov.nysenate.sage.provider.cityzip;

import java.util.List;

public interface CityZipService {
    List<Integer> getZipsByCity(String city);
}

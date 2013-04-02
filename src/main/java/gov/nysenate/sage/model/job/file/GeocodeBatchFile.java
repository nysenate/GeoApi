package gov.nysenate.sage.model.job.file;

import gov.nysenate.sage.model.address.Address;

import java.util.ArrayList;
import java.util.List;

/**
 * This batch file type is used primarily for very simple geocoding tests.
 */
public class GeocodeBatchFile extends BaseJobFile<GeocodeBatchRecord>
{
    public List<Address> getAddresses()
    {
        List<Address> addresses = new ArrayList<>();
        for (GeocodeBatchRecord g : this.records) {
            addresses.add(g.toAddress());
        }
        return addresses;
    }
}

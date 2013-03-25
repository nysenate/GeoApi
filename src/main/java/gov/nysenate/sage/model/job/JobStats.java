package gov.nysenate.sage.model.job;

import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.service.district.DistrictService;
import gov.nysenate.sage.service.geo.GeocodeService;

import java.util.Map;

public class JobStats
{
    private int processId;
    private Map<AddressService, Integer> addressCorrections;
    private Map<GeocodeService, Integer> geocodeCounts;
    private Map<DistrictService, Integer> districtAssignCounts;

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public Map<AddressService, Integer> getAddressCorrections() {
        return addressCorrections;
    }

    public void setAddressCorrections(Map<AddressService, Integer> addressCorrections) {
        this.addressCorrections = addressCorrections;
    }

    public Map<GeocodeService, Integer> getGeocodeCounts() {
        return geocodeCounts;
    }

    public void setGeocodeCounts(Map<GeocodeService, Integer> geocodeCounts) {
        this.geocodeCounts = geocodeCounts;
    }

    public Map<DistrictService, Integer> getDistrictAssignCounts() {
        return districtAssignCounts;
    }

    public void setDistrictAssignCounts(Map<DistrictService, Integer> districtAssignCounts) {
        this.districtAssignCounts = districtAssignCounts;
    }
}

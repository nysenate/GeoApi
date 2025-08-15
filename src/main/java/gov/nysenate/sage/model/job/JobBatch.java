package gov.nysenate.sage.model.job;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a subset of job records that belong to a job file.
 */
public record JobBatch(List<JobRecord> jobRecords, int fromRecord, int toRecord) {
    /**
     * Retrieve list of input addresses, using the USPS corrected versions if they exist.
     * @return List<Address>
     */
    public List<Address> getBestAddresses() {
        List<Address> addresses = new ArrayList<>();
        for (JobRecord jobRecord : jobRecords) {
            if (jobRecord.getCorrectedAddress() != null && jobRecord.getCorrectedAddress().isUspsValidated()) {
                addresses.add(jobRecord.getCorrectedAddress());
            } else {
                addresses.add(jobRecord.getAddress());
            }
        }
        return addresses;
    }

    /**
     * Retrieve list of geocoded addresses for this batch.
     * @return List<GeocodedAddress>
     */
    public List<GeocodedAddress> getGeocodedAddresses() {
        List<GeocodedAddress> geocodedAddresses = new ArrayList<>();
        for (JobRecord jobRecord : jobRecords) {
            geocodedAddresses.add(jobRecord.getGeocodedAddress());
        }
        return geocodedAddresses;
    }

    public void setAddressResults(List<AddressResult> addressResults) {
        if (jobRecords.size() != addressResults.size()) {
            return;
        }
        for (int i = 0; i < addressResults.size(); i++) {
            if (jobRecords.get(i) != null) {
                jobRecords.get(i).applyAddressResult(addressResults.get(i));
            }
        }
    }

    public void setGeocodeResults(List<GeocodeResult> geocodeResults) {
        if (jobRecords.size() != geocodeResults.size()) {
            return;
        }
        for (int i = 0; i < geocodeResults.size(); i++) {
            if (jobRecords.get(i) != null) {
                jobRecords.get(i).applyGeocodeResult(geocodeResults.get(i));
            }
        }
    }

    public void setDistrictResults(List<DistrictResult> districtResults) {
        if (jobRecords.size() != districtResults.size()) {
            return;
        }
        for (int i = 0; i < districtResults.size(); i++) {
            if (jobRecords.get(i) != null) {
                jobRecords.get(i).applyDistrictResult(districtResults.get(i));
            }
        }
    }
}

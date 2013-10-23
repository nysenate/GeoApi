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
public class JobBatch
{
    private List<JobRecord> jobRecords;
    private int fromRecord;
    private int toRecord;

    public JobBatch(List<JobRecord> jobRecords, int fromRecord, int toRecord) {
        this.jobRecords = jobRecords;
        this.fromRecord = fromRecord;
        this.toRecord = toRecord;
    }

    public List<JobRecord> getJobRecords() {
        return jobRecords;
    }

    public int getFromRecord() {
        return fromRecord;
    }

    public int getToRecord() {
        return toRecord;
    }

    /**
     * Retrieve list of input addresses for this batch.
     * @return List<Address>
     */
    public List<Address> getAddresses() {
        List<Address> addresses = new ArrayList<>();
        for (JobRecord jobRecord : jobRecords) {
            addresses.add(jobRecord.getAddress());
        }
        return addresses;
    }

    /**
     * Retrieve list of input addresses with option to instead return the usps corrected
     * versions if they exist.
     * @param swapWithValidatedAddress if true, perform swapping. otherwise delegate to getAddresses().
     * @return List<Address>
     */
    public List<Address> getAddresses(boolean swapWithValidatedAddress) {
        if (!swapWithValidatedAddress) {
            return getAddresses();
        }
        List<Address> addresses = new ArrayList<>();
        for (JobRecord jobRecord : jobRecords) {
            if (jobRecord.getCorrectedAddress() != null && jobRecord.getCorrectedAddress().isUspsValidated()) {
                addresses.add(jobRecord.getCorrectedAddress());
            }
            else {
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

    public void setAddressResult(int index, AddressResult addressResult) {
        if (this.jobRecords.get(index) != null) {
            this.jobRecords.get(index).applyAddressResult(addressResult);
        }
    }

    public void setGeocodeResult(int index, GeocodeResult geocodeResult) {
        if (this.jobRecords.get(index) != null) {
            this.jobRecords.get(index).applyGeocodeResult(geocodeResult);
        }
    }

    public void setDistrictResult(int index, DistrictResult districtResult) {
        if (this.jobRecords.get(index) != null) {
            this.jobRecords.get(index).applyDistrictResult(districtResult);
        }
    }
}
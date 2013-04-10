package gov.nysenate.sage.model.job;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
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

    public List<Address> getAddresses() {
        List<Address> addresses = new ArrayList<>();
        for (JobRecord jobRecord : jobRecords) {
            addresses.add(jobRecord.getAddress());
        }
        return addresses;
    }

    public List<GeocodedAddress> getGeocodedAddresses() {
        List<GeocodedAddress> geocodedAddresses = new ArrayList<>();
        for (JobRecord jobRecord : jobRecords) {
            geocodedAddresses.add(jobRecord.getGeocodedAddress());
        }
        return geocodedAddresses;
    }

    public void setGeocodeResult(int index, GeocodeResult geocodeResult) {
        this.jobRecords.get(index).applyGeocodeResult(geocodeResult);
    }

    public void setDistrictResult(int index, DistrictResult districtResult) {
        this.jobRecords.get(index).applyDistrictResult(districtResult);
    }
}
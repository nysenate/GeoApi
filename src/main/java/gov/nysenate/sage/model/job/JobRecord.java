package gov.nysenate.sage.model.job;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gov.nysenate.sage.model.job.JobFile.Column;

public class JobRecord
{
    protected List<Object> row;
    protected Map<Column, Integer> indexMap;
    protected Map<Column, Object> dataMap = new HashMap<>();

    protected Address address;
    protected Address correctedAddress;
    protected StreetAddress streetAddress;
    protected Geocode geocode;
    protected DistrictInfo districtInfo;

    public JobRecord()
    {
        this.address = new Address();
        this.correctedAddress = new Address();
        this.streetAddress = new StreetAddress();
        this.geocode = new Geocode();
        this.districtInfo = new DistrictInfo();
    }

    public JobRecord(JobFile parentJobFile, List<Object> row)
    {
        this.row = row;
        this.indexMap = parentJobFile.getColumnIndexMap();
        for (Column column : this.indexMap.keySet()) {
            Object value = this.row.get(this.indexMap.get(column));
            dataMap.put(column, value);
        }

        /** Construct address */
        String street = (String) dataMap.get(Column.street);
        String city = (String) dataMap.get(Column.city);
        String state = (String) dataMap.get(Column.state);
        String zip5 = (String) dataMap.get(Column.zip5);
        String zip4 = (String) dataMap.get(Column.zip4);
        this.address = new Address(street, "", city, state, zip5, zip4);
    }

    public List<Object> getRow()
    {
        for (Column column : this.indexMap.keySet()) {
            this.row.set(this.indexMap.get(column), this.dataMap.get(column));
        }
        return this.row;
    }

    public Map<Column, Object> getDataMap()
    {
        return dataMap;
    }

    public void applyAddressResult(AddressResult addressResult)
    {
        if (addressResult != null && addressResult.isValidated() && addressResult.getAddress() != null) {
            this.correctedAddress = addressResult.getAddress();

            this.dataMap.put(Column.uspsStreet, this.correctedAddress.getAddr1());
            this.dataMap.put(Column.uspsCity, this.correctedAddress.getPostalCity());
            this.dataMap.put(Column.uspsState, this.correctedAddress.getState());
            this.dataMap.put(Column.uspsZip5, this.correctedAddress.getZip5());
            this.dataMap.put(Column.uspsZip4, this.correctedAddress.getZip4());
        }
    }

    public void applyGeocodeResult(GeocodeResult geocodeResult)
    {
        if (geocodeResult != null && geocodeResult.isSuccess() && geocodeResult.getGeocodedAddress() != null) {
            GeocodedAddress geocodedAddress = geocodeResult.getGeocodedAddress();
            this.geocode = geocodedAddress.getGeocode();

            this.dataMap.put(Column.lat, this.geocode.getLat());
            this.dataMap.put(Column.lon, this.geocode.getLon());
            this.dataMap.put(Column.geoMethod, this.geocode.getMethod());
            this.dataMap.put(Column.geoQuality, this.geocode.getQuality());
        }
    }

    public void applyDistrictResult(DistrictResult districtResult)
    {
        if (districtResult != null && districtResult.isSuccess()) {
            this.districtInfo = districtResult.getDistrictInfo();
            this.dataMap.put(Column.senate, districtInfo.getDistCode(DistrictType.SENATE));
            this.dataMap.put(Column.assembly, districtInfo.getDistCode(DistrictType.ASSEMBLY));
            this.dataMap.put(Column.congressional, districtInfo.getDistCode(DistrictType.CONGRESSIONAL));
            this.dataMap.put(Column.county, districtInfo.getDistCode(DistrictType.COUNTY));
            this.dataMap.put(Column.school, districtInfo.getDistCode(DistrictType.SCHOOL));
            // Ensures the town isn't overwritten
            String townCode = districtInfo.getDistCode(DistrictType.TOWN_CITY);
            if (townCode != null) {
                this.dataMap.put(Column.town, townCode);
            }
            this.dataMap.put(Column.election, districtInfo.getDistCode(DistrictType.ELECTION));
            this.dataMap.put(Column.ward, districtInfo.getDistCode(DistrictType.WARD));
        }
    }

    /** Explicit getters/setters */
    public Address getAddress()
    {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Address getCorrectedAddress() {
        return correctedAddress;
    }

    public void setCorrectedAddress(Address correctedAddress) {
        this.correctedAddress = correctedAddress;
    }

    public StreetAddress getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(StreetAddress streetAddress) {
        this.streetAddress = streetAddress;
    }

    public Geocode getGeocode() {
        return geocode;
    }

    public void setGeocode(Geocode geocode) {
        this.geocode = geocode;
    }

    public DistrictInfo getDistrictInfo() {
        return districtInfo;
    }

    public void setDistrictInfo(DistrictInfo districtInfo) {
        this.districtInfo = districtInfo;
    }

    /** Implicit getters */
    public GeocodedAddress getGeocodedAddress() {
        boolean hasCorrectedAddress = this.correctedAddress != null && !this.correctedAddress.isEmpty();
        return (geocode != null) ?  new GeocodedAddress((hasCorrectedAddress ? correctedAddress : address), geocode) : new GeocodedAddress();
    }
}

package gov.nysenate.sage.model.job;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
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

public class JobRecord {
    private final List<Object> row;
    private final Map<Column, Integer> indexMap;
    private final Map<Column, Object> dataMap = new HashMap<>();

    private final Address address;
    private Address correctedAddress;
    private Geocode geocode;
    private DistrictInfo districtInfo;

    public JobRecord(Map<Column, Integer> indexMap, List<Object> row) {
        this.indexMap = indexMap;
        this.row = row;
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
        this.address = Address.getAddress(street, "", city, state, zip5, zip4);
    }

    public List<Object> getRow() {
        for (Column column : this.indexMap.keySet()) {
            this.row.set(this.indexMap.get(column), this.dataMap.get(column));
        }
        return this.row;
    }

    public void applyAddressResult(AddressResult addressResult) {
        if (addressResult != null && addressResult.isValidated() && addressResult.getAddress() != null) {
            this.correctedAddress = addressResult.getAddress();

            this.dataMap.put(Column.uspsStreet, this.correctedAddress.getAddr1());
            this.dataMap.put(Column.uspsCity, this.correctedAddress.getPostalCity());
            this.dataMap.put(Column.uspsState, this.correctedAddress.getState());
            this.dataMap.put(Column.uspsZip5, this.correctedAddress.getZip5().toString());
            this.dataMap.put(Column.uspsZip4, this.correctedAddress.getZip4().toString());
        }
    }

    public void applyGeocodeResult(GeocodeResult geocodeResult) {
        if (geocodeResult != null && geocodeResult.isSuccess() && geocodeResult.getGeocodedAddress() != null) {
            GeocodedAddress geocodedAddress = geocodeResult.getGeocodedAddress();
            this.geocode = geocodedAddress.getGeocode();

            this.dataMap.put(Column.lat, this.geocode.lat());
            this.dataMap.put(Column.lon, this.geocode.lon());
            this.dataMap.put(Column.geoMethod, this.geocode.originalGeocoder());
            this.dataMap.put(Column.geoQuality, this.geocode.quality());
        }
    }

    public void applyDistrictResult(DistrictResult districtResult) {
        if (districtResult != null && districtResult.isSuccess()) {
            this.districtInfo = districtResult.getDistrictInfo();
            this.dataMap.put(Column.senate, districtInfo.getDistCode(DistrictType.SENATE));
            this.dataMap.put(Column.assembly, districtInfo.getDistCode(DistrictType.ASSEMBLY));
            this.dataMap.put(Column.congressional, districtInfo.getDistCode(DistrictType.CONGRESSIONAL));
            this.dataMap.put(Column.county, districtInfo.getDistCode(DistrictType.COUNTY));
            this.dataMap.put(Column.school, districtInfo.getDistCode(DistrictType.SCHOOL));
            this.dataMap.put(Column.town_city, districtInfo.getDistCode(DistrictType.TOWN_CITY));
            this.dataMap.put(Column.election, districtInfo.getDistCode(DistrictType.ELECTION));
            this.dataMap.put(Column.ward, districtInfo.getDistCode(DistrictType.WARD));
        }
    }

    /** Explicit getters/setters */
    public Address getAddress() {
        return address;
    }

    public Address getCorrectedAddress() {
        return correctedAddress;
    }

    /** Implicit getters */
    public GeocodedAddress getGeocodedAddress() {
        boolean hasCorrectedAddress = correctedAddress != null && correctedAddress.isValid();
        return (geocode != null) ?  new GeocodedAddress((hasCorrectedAddress ? correctedAddress : address), geocode) : new GeocodedAddress();
    }
}

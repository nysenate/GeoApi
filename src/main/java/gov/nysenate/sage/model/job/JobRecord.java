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
    private GeocodedAddress geocodedAddress;

    public JobRecord(Map<Column, Integer> indexMap, List<Object> row) {
        this.indexMap = indexMap;
        this.row = row;
        for (Column column : indexMap.keySet()) {
            Object value = row.get(indexMap.get(column));
            dataMap.put(column, value);
        }

        String street = (String) dataMap.get(Column.street);
        String city = (String) dataMap.get(Column.city);
        String state = (String) dataMap.get(Column.state);
        String zip5 = (String) dataMap.get(Column.zip5);
        String zip4 = (String) dataMap.get(Column.zip4);
        this.address = Address.getAddress(street, "", city, state, zip5, zip4);
    }

    public List<Object> getRow() {
        for (Column column : indexMap.keySet()) {
            row.set(indexMap.get(column), dataMap.get(column));
        }
        return row;
    }

    public void applyAddressResult(AddressResult addressResult) {
        if (addressResult != null && addressResult.isValidated() && addressResult.getAddress() != null) {
            this.correctedAddress = addressResult.getAddress();

            dataMap.put(Column.uspsStreet, correctedAddress.getAddr1());
            dataMap.put(Column.uspsCity, correctedAddress.getPostalCity());
            dataMap.put(Column.uspsState, correctedAddress.getState());
            dataMap.put(Column.uspsZip5, correctedAddress.getZip5().toString());
            dataMap.put(Column.uspsZip4, correctedAddress.getZip4().toString());
        }
    }

    public void applyGeocodeResult(GeocodeResult geocodeResult) {
        if (geocodeResult == null) {
            return;
        }
        this.geocodedAddress = geocodeResult.getGeocodedAddress();
        if (geocodedAddress != null) {
            Geocode geocode = geocodedAddress.getGeocode();
            if (geocode != null) {
                dataMap.put(Column.lat, geocode.lat());
                dataMap.put(Column.lon, geocode.lon());
                dataMap.put(Column.geoMethod, geocode.originalGeocoder());
                dataMap.put(Column.geoQuality, geocode.quality());
            }
        }
    }

    public void applyDistrictResult(DistrictResult districtResult) {
        if (districtResult != null && districtResult.isSuccess()) {
            DistrictInfo districtInfo = districtResult.getDistrictInfo();
            dataMap.put(Column.senate, districtInfo.getDistCode(DistrictType.SENATE));
            dataMap.put(Column.assembly, districtInfo.getDistCode(DistrictType.ASSEMBLY));
            dataMap.put(Column.congressional, districtInfo.getDistCode(DistrictType.CONGRESSIONAL));
            dataMap.put(Column.county, districtInfo.getDistCode(DistrictType.COUNTY));
            dataMap.put(Column.school, districtInfo.getDistCode(DistrictType.SCHOOL));
            dataMap.put(Column.town_city, districtInfo.getDistCode(DistrictType.TOWN_CITY));
            dataMap.put(Column.election, districtInfo.getDistCode(DistrictType.ELECTION));
            dataMap.put(Column.ward, districtInfo.getDistCode(DistrictType.WARD));
        }
    }

    public Address getAddress() {
        return address;
    }

    public Address getCorrectedAddress() {
        return correctedAddress;
    }

    public GeocodedAddress getGeocodedAddress() {
        return geocodedAddress;
    }
}

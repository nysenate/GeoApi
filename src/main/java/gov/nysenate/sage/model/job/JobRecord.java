package gov.nysenate.sage.model.job;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class JobRecord {
    private static final Logger logger = LoggerFactory.getLogger(JobRecord.class);

    private final List<Object> row;
    private final Map<Column, Integer> indexMap;
    private final Map<Column, Object> dataMap = new HashMap<>();

    private final Address address;
    private Address correctedAddress;
    private GeocodedAddress geocodedAddress;
    private DistrictMatchLevel matchLevel;

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
        Address tempAddr;
        // Ensures basic JobRecord creation occurs.
        try {
            tempAddr = new Address(street, "", city, state, zip5, zip4);
        } catch (Exception ex) {
            logger.warn(ex.getMessage());
            try {
                tempAddr = new Address(street, "", city, state, zip5, "");
            } catch (Exception ex2) {
                tempAddr = new Address(street, "", city, state, "", "");
            }
        }
        this.address = tempAddr;
    }

    public List<Object> getRow() {
        for (Column column : indexMap.keySet()) {
            Object colValue = dataMap.get(column);
            row.set(indexMap.get(column), colValue == null ? null : colValue.toString());
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
            if (correctedAddress.getZip4() != null) {
                dataMap.put(Column.uspsZip4, correctedAddress.getZip4().toString());
            }
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
            this.matchLevel = districtInfo.matchLevel();
            for (Column column : Column.values()) {
                if (column.group() != Column.Group.district) {
                    continue;
                }
                dataMap.put(column, districtInfo.getDistCode(DistrictType.valueOf(column.name().toUpperCase())));
            }
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

    public DistrictMatchLevel getMatchLevel() {
        return matchLevel;
    }

    public Set<Column> getAssignedDistricts() {
        var assignedDistricts = EnumSet.noneOf(Column.class);
        for (Column column : indexMap.keySet()) {
            if (column.group() == Column.Group.district && dataMap.get(column) != null) {
                assignedDistricts.add(column);
            }
        }
        return assignedDistricts;
    }
}

package gov.nysenate.sage.scripts.streetfinder.scripts.nysaddresspoints;

import com.univocity.parsers.annotations.Parsed;
import gov.nysenate.sage.model.address.Address;

public class NYSAddressPoint {
    @Parsed(field = "OID_")
    public String oid;
    @Parsed(field = "PrefixAddressNumber")
    public String prefixAddressNumber;
    @Parsed(field = "AddressNumber")
    public String addressNumber;
    @Parsed(field = "SuffixAddressNumber")
    public String suffixAddressNumber;
    @Parsed(field = "StreetName")
    public String streetName;
    @Parsed(field = "SubAddress")
    public String subAddress;
    @Parsed(field = "ZipName")
    public String zipName;
    @Parsed(field = "State")
    public String state;
    @Parsed(field = "ZipCode")
    public String zipCode;
    @Parsed(field = "PointType")
    public String pointType;
    @Parsed(field = "CityTownName")
    public String cityTownName;
    @Parsed(field = "PlaceName")
    public String placeName;
    @Parsed(field = "PlaceType")
    public String placeType;
    @Parsed(field = "CompleteStreetName")
    public String completeStreetName;
    @Parsed(field = "Status")
    public String status;
    @Parsed(field = "AddressLabel")
    public String addressLabel;
    @Parsed(field = "CountyName")
    public String countyName;
    @Parsed(field = "Country")
    public String country;
    @Parsed(field = "IncorporatedMunicipality")
    public String incorporatedMunicipality;
    @Parsed(field = "UnincorporatedMunicipality")
    public String unincorporatedMunicipality;
    @Parsed(field = "MunicipalityType")
    public String municipalityType;
    @Parsed(field = "AddressType")
    public String addressType;
    @Parsed(field = "Latitude")
    public String latitude;
    @Parsed(field = "Longitude")
    public String longitude;
    @Parsed(field = "SenateDistrict")
    public String senateDistrict;
    @Parsed(field = "AssemblyDistrict")
    public String assemblyDistrict;
    @Parsed(field = "ConDistrict")
    public String congressionalDistrict;

    public NYSAddressPoint() {
    }

    public NYSAddressPoint(String[] columns) {
        oid = columns[0];
        prefixAddressNumber = columns[1];
        addressNumber = columns[2];
        suffixAddressNumber = columns[3];
        streetName = columns[4];
        subAddress = columns[5];
        zipName = columns[6];
        state = columns[7];
        zipCode = columns[8];
        pointType = columns[9];
        cityTownName = columns[10];
        placeName = columns[11];
        placeType = columns[12];
        completeStreetName = columns[13];
        status = columns[14];
        addressLabel = columns[15];
        countyName = columns[16];
        country = columns[17];
        incorporatedMunicipality = columns[18];
        unincorporatedMunicipality = columns[19];
        municipalityType = columns[20];
        addressType = columns[21];
        latitude = columns[22];
        longitude = columns[23];
        senateDistrict = columns[24];
        assemblyDistrict = columns[25];
        congressionalDistrict = columns[26];
    }

    public Address toAddress() {
        return new Address(addressLabel, cityTownName, state, zipCode);
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getPrefixAddressNumber() {
        return prefixAddressNumber;
    }

    public void setPrefixAddressNumber(String prefixAddressNumber) {
        this.prefixAddressNumber = prefixAddressNumber;
    }

    public String getAddressNumber() {
        return addressNumber;
    }

    public void setAddressNumber(String addressNumber) {
        this.addressNumber = addressNumber;
    }

    public String getSuffixAddressNumber() {
        return suffixAddressNumber;
    }

    public void setSuffixAddressNumber(String suffixAddressNumber) {
        this.suffixAddressNumber = suffixAddressNumber;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getSubAddress() {
        return subAddress;
    }

    public void setSubAddress(String subAddress) {
        this.subAddress = subAddress;
    }

    public String getZipName() {
        return zipName;
    }

    public void setZipName(String zipName) {
        this.zipName = zipName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getPointType() {
        return pointType;
    }

    public void setPointType(String pointType) {
        this.pointType = pointType;
    }

    public String getCityTownName() {
        return cityTownName;
    }

    public void setCityTownName(String cityTownName) {
        this.cityTownName = cityTownName;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceType() {
        return placeType;
    }

    public void setPlaceType(String placeType) {
        this.placeType = placeType;
    }

    public String getCompleteStreetName() {
        return completeStreetName;
    }

    public void setCompleteStreetName(String completeStreetName) {
        this.completeStreetName = completeStreetName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAddressLabel() {
        return addressLabel;
    }

    public void setAddressLabel(String addressLabel) {
        this.addressLabel = addressLabel;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getIncorporatedMunicipality() {
        return incorporatedMunicipality;
    }

    public void setIncorporatedMunicipality(String incorporatedMunicipality) {
        this.incorporatedMunicipality = incorporatedMunicipality;
    }

    public String getUnincorporatedMunicipality() {
        return unincorporatedMunicipality;
    }

    public void setUnincorporatedMunicipality(String unincorporatedMunicipality) {
        this.unincorporatedMunicipality = unincorporatedMunicipality;
    }

    public String getMunicipalityType() {
        return municipalityType;
    }

    public void setMunicipalityType(String municipalityType) {
        this.municipalityType = municipalityType;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getSenateDistrict() {
        return senateDistrict;
    }

    public void setSenateDistrict(String senateDistrict) {
        this.senateDistrict = senateDistrict;
    }

    public String getAssemblyDistrict() {
        return assemblyDistrict;
    }

    public void setAssemblyDistrict(String assemblyDistrict) {
        this.assemblyDistrict = assemblyDistrict;
    }

    public String getCongressionalDistrict() {
        return congressionalDistrict;
    }

    public void setCongressionalDistrict(String congressionalDistrict) {
        this.congressionalDistrict = congressionalDistrict;
    }

    @Override
    public String toString() {
        return "NYSAddressPoint{" +
                "oid='" + oid + '\'' +
                ", prefixAddressNumber='" + prefixAddressNumber + '\'' +
                ", addressNumber='" + addressNumber + '\'' +
                ", suffixAddressNumber='" + suffixAddressNumber + '\'' +
                ", streetName='" + streetName + '\'' +
                ", subAddress='" + subAddress + '\'' +
                ", zipName='" + zipName + '\'' +
                ", state='" + state + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", pointType='" + pointType + '\'' +
                ", cityTownName='" + cityTownName + '\'' +
                ", placeName='" + placeName + '\'' +
                ", placeType='" + placeType + '\'' +
                ", completeStreetName='" + completeStreetName + '\'' +
                ", staus='" + status + '\'' +
                ", addressLabel='" + addressLabel + '\'' +
                ", countyName='" + countyName + '\'' +
                ", country='" + country + '\'' +
                ", incorporatedMunicipality='" + incorporatedMunicipality + '\'' +
                ", unincorporatedMunicipality='" + unincorporatedMunicipality + '\'' +
                ", municipalityType='" + municipalityType + '\'' +
                ", addressType='" + addressType + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", senateDistrict='" + senateDistrict + '\'' +
                ", assemblyDistrict='" + assemblyDistrict + '\'' +
                ", congressionalDistrict='" + congressionalDistrict + '\'' +
                '}';
    }
}

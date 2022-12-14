package gov.nysenate.sage.scripts.streetfinder.scripts.nysaddresspoints;

import gov.nysenate.sage.model.address.Address;

public class NYSAddressPoint {
  public String oid;
  public String prefixAddressNumber;
  public String addressNumber;
  public String suffixAddressNumber;
  public String streetName;
  public String subAddress;
  public String zipName;
  public String state;
  public String zipCode;
  public String pointType;
  public String cityTownName;
  public String placeName;
  public String placeType;
  public String completeStreetName;
  public String status;
  public String addressLabel;
  public String countyName;
  public String country;
  public String incorporatedMunicipality;
  public String unincorporatedMunicipality;
  public String municipalityType;
  public String addressType;
  public String latitude;
  public String longitude;
  public String senateDistrict;
  public String assemblyDistrict;
  public String congressionalDistrict;

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

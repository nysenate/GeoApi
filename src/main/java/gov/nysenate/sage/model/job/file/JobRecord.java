package gov.nysenate.sage.model.job.file;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.Point;

import java.util.HashMap;

public class JobRecord
{
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

    public String getStreet() {
        return address.getAddr1();
    }

    public void setStreet(String street) {
        this.address.setAddr1(street);
    }

    public String getCity() {
        return address.getCity();
    }

    public void setCity(String city) {
        this.address.setCity(city);
    }

    public String getState() {
        return address.getState();
    }

    public void setState(String state) {
        this.address.setState(state);
    }

    public String getZip5() {
        return address.getZip5();
    }

    public void setZip5(String zip5) {
        this.address.setZip5(zip5);
    }

    public String getZip4() {
        return address.getZip4();
    }

    public void setZip4(String zip4) {
        this.address.setZip4(zip4);
    }

    public Integer getStreetNumber() {
        return streetAddress.getBldgNum();
    }

    public void setStreetNumber(Integer streetNumber) {
        this.streetAddress.setBldgNum(streetNumber);
    }

    public String getStreetName() {
        return streetAddress.getStreet();
    }

    public void setStreetName(String streetName) {
        this.streetAddress.setStreet(streetName);
    }

    public Double getGeocode1() {
        return geocode.getLat();
    }

    public void setGeocode1(Double geocode1) {
        this.geocode.setLat(geocode1);
    }

    public Double getGeocode2() {
        return geocode.getLon();
    }

    public void setGeocode2(Double geocode2) {
        this.geocode.setLon(geocode2);
    }

    public Double getLat() {
        return geocode.getLat();
    }

    public void setLat(Double lat) {
        geocode.setLat(lat);
    }

    public Double getLon() {
        return geocode.getLon();
    }

    public void setLon(Double lon) {
        this.geocode.setLon(lon);
    }

    public String getGeoMethod() {
        return geocode.getMethod();
    }

    public void setGeoMethod(String geoMethod) {
        this.geocode.setMethod(geoMethod);
    }

    public String getQuality() {
        return geocode.getQuality().name();
    }

    public String getTown() {
        return districtInfo.getDistCode(DistrictType.TOWN);
    }

    public void setTown(String town) {
        this.districtInfo.setDistCode(DistrictType.TOWN, town);
    }

    public String getWard() {
        return districtInfo.getDistCode(DistrictType.WARD);
    }

    public void setWard(String ward) {
        this.districtInfo.setDistCode(DistrictType.WARD, ward);
    }

    public String getElection() {
        return districtInfo.getDistCode(DistrictType.ELECTION);
    }

    public void setElection(String election) {
        this.districtInfo.setDistCode(DistrictType.ELECTION, election);
    }

    public String getCongressional() {
        return districtInfo.getDistCode(DistrictType.CONGRESSIONAL);
    }

    public void setCongressional(String congressional) {
        this.districtInfo.setDistCode(DistrictType.CONGRESSIONAL, congressional);
    }

    public String getSenate() {
        return districtInfo.getDistCode(DistrictType.SENATE);
    }

    public void setSenate(String senate) {
        this.districtInfo.setDistCode(DistrictType.SENATE, senate);
    }

    public String getAssembly() {
        return districtInfo.getDistCode(DistrictType.ASSEMBLY);
    }

    public void setAssembly(String assembly) {
        this.districtInfo.setDistCode(DistrictType.ASSEMBLY, assembly);
    }

    public String getCounty() {
        return districtInfo.getDistCode(DistrictType.COUNTY);
    }

    public void setCounty(String county) {
        this.districtInfo.setDistCode(DistrictType.COUNTY, county);
    }

    public String getSchool() {
        return districtInfo.getDistCode(DistrictType.SCHOOL);
    }

    public void setSchool(String school) {
        this.districtInfo.setDistCode(DistrictType.SCHOOL, school);
    }
}

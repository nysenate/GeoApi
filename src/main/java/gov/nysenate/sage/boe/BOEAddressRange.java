package gov.nysenate.sage.boe;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;

public class BOEAddressRange extends BOEAddress implements java.io.Serializable {

    public static void main(String[] args) throws Exception {
        BeanInfo info = Introspector.getBeanInfo(BOEAddressRange.class);
        for (MethodDescriptor d : info.getMethodDescriptors()) {
            System.out.println(d);
        }
    }

    public int id;

    public int bldgLoNum;
    public String bldgLoChr;
    public int bldgHiNum;
    public String bldgHiChr;
    public String bldgParity;

    public int aptLoNum;
    public String aptLoChr;
    public int aptHiNum;
    public String aptHiChr;
    public String aptParity;

    @Override
    public String toString() {
        return id+" "+bldgLoNum+(bldgLoChr!=null ? bldgLoChr : "")+" - "+bldgHiNum+(bldgHiChr!=null ? bldgHiChr : "")+" ("+bldgParity+") "+street+" "+town+" "+zip5;
    }

    public boolean isValid() {
        return street != null && street.length() != 0 && !street.contains("UNKNOWN");
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getStreet() {
        return street;
    }
    public void setStreet(String street) {
        this.street = street;
    }
    public String getTown() {
        return town;
    }
    public void setTown(String town) {
        this.town = town;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public int getZip5() {
        return zip5;
    }
    public void setZip5(int zip5) {
        this.zip5 = zip5;
    }
    public int getBldgLoNum() {
        return bldgLoNum;
    }
    public void setBldgLoNum(int bldgLoNum) {
        this.bldgLoNum = bldgLoNum;
    }
    public String getBldgLoChr() {
        return bldgLoChr;
    }
    public void setBldgLoChr(String bldgLoChr) {
        this.bldgLoChr = bldgLoChr;
    }
    public int getBldgHiNum() {
        return bldgHiNum;
    }
    public void setBldgHiNum(int bldgHiNum) {
        this.bldgHiNum = bldgHiNum;
    }
    public String getBldgHiChr() {
        return bldgHiChr;
    }
    public void setBldgHiChr(String bldgHiChr) {
        this.bldgHiChr = bldgHiChr;
    }
    public int getAptLoNum() {
        return aptLoNum;
    }
    public void setAptLoNum(int aptLoNum) {
        this.aptLoNum = aptLoNum;
    }
    public String getAptLoChr() {
        return aptLoChr;
    }
    public void setAptLoChr(String aptLoChr) {
        this.aptLoChr = aptLoChr;
    }
    public int getAptHiNum() {
        return aptHiNum;
    }
    public void setAptHiNum(int aptHiNum) {
        this.aptHiNum = aptHiNum;
    }
    public String getAptHiChr() {
        return aptHiChr;
    }
    public void setAptHiChr(String aptHiChr) {
        this.aptHiChr = aptHiChr;
    }
    public int getElectionCode() {
        return electionCode;
    }
    public void setElectionCode(int electionCode) {
        this.electionCode = electionCode;
    }
    public int getCountyCode() {
        return countyCode;
    }
    public void setCountyCode(int countyCode) {
        this.countyCode = countyCode;
    }
    public int getAssemblyCode() {
        return assemblyCode;
    }
    public void setAssemblyCode(int assemblyCode) {
        this.assemblyCode = assemblyCode;
    }
    public int getSenateCode() {
        return senateCode;
    }
    public void setSenateCode(int senateCode) {
        this.senateCode = senateCode;
    }
    public int getCongressionalCode() {
        return congressionalCode;
    }
    public void setCongressionalCode(int congressionalCode) {
        this.congressionalCode = congressionalCode;
    }
    public String getTownCode() {
        return townCode;
    }
    public void setTownCode(String townCode) {
        this.townCode = townCode;
    }
    public String getWardCode() {
        return wardCode;
    }
    public void setWardCode(String wardCode) {
        this.wardCode = wardCode;
    }
    public String getSchoolCode() {
        return schoolCode;
    }
    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }
    public String getClegCode() {
        return clegCode;
    }
    public void setClegCode(String clegCode) {
        this.clegCode = clegCode;
    }
    public String getCityCode() {
        return cityCode;
    }
    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }
    public String getFireCode() {
        return fireCode;
    }
    public void setFireCode(String fireCode) {
        this.fireCode = fireCode;
    }
    public String getVillCode() {
        return villCode;
    }
    public void setVillCode(String villCode) {
        this.villCode = villCode;
    }
    public String getAptParity() {
        return aptParity;
    }
    public void setAptParity(String aptParity) {
        this.aptParity = aptParity;
    }
    public String getBldgParity() {
        return bldgParity;
    }
    public void setBldgParity(String bldgParity) {
        this.bldgParity = bldgParity;
    }
}

package gov.nysenate.sage.model.district;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DistrictInfo is used to represent the various district names
 * and codes at a particular location.
 */
public class DistrictInfo
{
    /** A set of the DistrictType's that were actually district assigned.
     *  Whenever a district is set using the constructor or setter methods,
     *  the district type will be added to this assigned districts set */
    protected Set<DistrictType> assignedDistricts = new LinkedHashSet<>();

    /** District Names */
    protected String congressionalName = "";
    protected String senateName = "";
    protected String assemblyName = "";
    protected String countyName = "";
    protected String townName = "";
    protected String schoolName = "";
    protected String wardName = "";
    protected String electionName = "";

    /** District Codes */
    protected int congressionalCode = 0;
    protected int senateCode = 0;
    protected int assemblyCode = 0;
    protected int countyCode = 0;
    protected String townCode = "";
    protected String schoolCode = "";
    protected int wardCode = 0;
    protected int electionCode = 0;

    /** District Maps */
    protected DistrictMap congressionalMap;
    protected DistrictMap senateMap;
    protected DistrictMap assemblyMap;
    protected DistrictMap countyMap;
    protected DistrictMap townMap;
    protected DistrictMap schoolMap;
    protected DistrictMap electionMap;

    /** Additional District Codes */
    protected int clegCode = 0;
    protected String cityCode = "";
    protected String fireCode = "";
    protected String villCode = "";

    public DistrictInfo() {}

    public DistrictInfo( int congressionalCode, int countyCode, int senateCode,
                         int assemblyCode, String townCode, String schoolCode )
    {
        this.setCongressionalCode(congressionalCode);
        this.setCountyCode(countyCode);
        this.setSenateCode(senateCode);
        this.setAssemblyCode(assemblyCode);
        this.setTownCode(townCode);
        this.setSchoolCode(schoolCode);
    }

    public Set<DistrictType> getAssignedDistricts() {
        return assignedDistricts;
    }

    protected void addAssignedDistrict(DistrictType assignedDistrict) {
        this.assignedDistricts.add(assignedDistrict);
    }

    public String getCongressionalName() {
        return congressionalName;
    }

    public void setCongressionalName(String congressionalName) {
        this.congressionalName = congressionalName;
    }

    public String getSenateName() {
        return senateName;
    }

    public void setSenateName(String senateName) {
        this.senateName = senateName;
    }

    public String getAssemblyName() {
        return assemblyName;
    }

    public void setAssemblyName(String assemblyName) {
        this.assemblyName = assemblyName;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getTownName() {
        return townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public String getElectionName() {
        return electionName;
    }

    public void setElectionName(String electionName) {
        this.electionName = electionName;
    }

    public int getCongressionalCode() {
        return congressionalCode;
    }

    public void setCongressionalCode(int congressionalCode) {
        if (congressionalCode > 0){
            this.congressionalCode = congressionalCode;
            this.addAssignedDistrict(DistrictType.CONGRESSIONAL);
        }
    }

    public int getCountyCode() {
        return countyCode;
    }

    public void setCountyCode(int countyCode) {
        if (countyCode > 0){
            this.countyCode = countyCode;
            this.addAssignedDistrict(DistrictType.COUNTY);
        }
    }

    public int getSenateCode() {
        return senateCode;
    }

    public void setSenateCode(int senateCode) {
        if (senateCode > 0){
            this.senateCode = senateCode;
            this.addAssignedDistrict(DistrictType.SENATE);
        }
    }

    public int getAssemblyCode() {
        return assemblyCode;
    }

    public void setAssemblyCode(int assemblyCode) {
        if (assemblyCode > 0) {
            this.assemblyCode = assemblyCode;
            this.addAssignedDistrict(DistrictType.ASSEMBLY);
        }
    }

    public int getElectionCode() {
        return electionCode;
    }

    public void setElectionCode(int electionCode) {
        if (electionCode > 0){
            this.electionCode = electionCode;
            this.addAssignedDistrict(DistrictType.ELECTION);
        }
    }

    public String getTownCode() {
        return townCode;
    }

    public void setTownCode(String townCode) {
        if (townCode != null && !townCode.isEmpty()){
            this.townCode = townCode;
            this.addAssignedDistrict(DistrictType.TOWN);
        }
    }

    public int getWardCode() {
        return wardCode;
    }

    public void setWardCode(int wardCode) {
        this.wardCode = wardCode;
    }

    public String getSchoolCode() {
        return schoolCode;
    }

    public void setSchoolCode(String schoolCode) {
        if (schoolCode != null && !schoolCode.isEmpty()){
            this.schoolCode = schoolCode;
            this.addAssignedDistrict(DistrictType.SCHOOL);
        }
    }

    public int getClegCode() {
        return clegCode;
    }

    public void setClegCode(int clegCode) {
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

    public DistrictMap getCongressionalMap() {
        return congressionalMap;
    }

    public void setCongressionalMap(DistrictMap congressionalMap) {
        this.congressionalMap = congressionalMap;
    }

    public DistrictMap getSenateMap() {
        return senateMap;
    }

    public void setSenateMap(DistrictMap senateMap) {
        this.senateMap = senateMap;
    }

    public DistrictMap getAssemblyMap() {
        return assemblyMap;
    }

    public void setAssemblyMap(DistrictMap assemblyMap) {
        this.assemblyMap = assemblyMap;
    }

    public DistrictMap getCountyMap() {
        return countyMap;
    }

    public void setCountyMap(DistrictMap countyMap) {
        this.countyMap = countyMap;
    }

    public DistrictMap getTownMap() {
        return townMap;
    }

    public void setTownMap(DistrictMap townMap) {
        this.townMap = townMap;
    }

    public DistrictMap getSchoolMap() {
        return schoolMap;
    }

    public void setSchoolMap(DistrictMap schoolMap) {
        this.schoolMap = schoolMap;
    }

    public DistrictMap getElectionMap() {
        return electionMap;
    }

    public void setElectionMap(DistrictMap electionMap) {
        this.electionMap = electionMap;
    }
}

package gov.nysenate.sage.model.district;

/**
 * DistrictInfo is used to represent the various district names
 * and codes at a particular location.
 */
public class DistrictInfo
{
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
    protected int countyCode = 0;
    protected int senateCode = 0;
    protected int assemblyCode = 0;
    protected int electionCode = 0;
    protected String townCode = "";
    protected int wardCode = 0;
    protected String schoolCode = "";
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
        this.congressionalCode = congressionalCode;
    }

    public int getCountyCode() {
        return countyCode;
    }

    public void setCountyCode(int countyCode) {
        this.countyCode = countyCode;
    }

    public int getSenateCode() {
        return senateCode;
    }

    public void setSenateCode(int senateCode) {
        this.senateCode = senateCode;
    }

    public int getAssemblyCode() {
        return assemblyCode;
    }

    public void setAssemblyCode(int assemblyCode) {
        this.assemblyCode = assemblyCode;
    }

    public int getElectionCode() {
        return electionCode;
    }

    public void setElectionCode(int electionCode) {
        this.electionCode = electionCode;
    }

    public String getTownCode() {
        return townCode;
    }

    public void setTownCode(String townCode) {
        this.townCode = townCode;
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
        this.schoolCode = schoolCode;
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
}

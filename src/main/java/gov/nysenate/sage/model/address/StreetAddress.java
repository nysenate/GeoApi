package gov.nysenate.sage.model.address;

/**
 * Represents a street address with address components broken down.
 */
public class StreetAddress
{
    protected int bldgNum;
    protected String preDir = "";
    protected String street = "";
    protected String streetType = "";
    protected String postDir = "";
    protected String bldgChar = "";
    protected int aptNum;
    protected String aptChar = "";
    protected String internal = "";
    protected String location = "";
    protected String state = "";
    protected String zip5 = "";
    protected String bldgParity = "";
    protected String aptParity = "";

    public StreetAddress(){}

    /**
     * Converts the street address into a basic Address object performing the
     * necessary null checks.
     * @return  Address
     */
    public Address toAddress()
    {
        String addr1 = "";
        if (getBldgNum() != 0) addr1 += Integer.toString(getBldgNum()) + " ";
        if (!getPreDir().isEmpty()) addr1 += getPreDir() + " ";
        if (!getStreet().isEmpty()) addr1 += getStreet() + " ";
        if (!getStreetType().isEmpty()) addr1 += getStreetType() + " ";
        if (!getPostDir().isEmpty()) addr1 += getPostDir() + " ";

        String addr2 = "";
        if (!getInternal().isEmpty()){
            addr2 = getInternal();
        }
        else if (!getBldgChar().isEmpty()){
            addr2 = getBldgChar();
        }
        else if (getAptNum() != 0) {
            addr2 += Integer.toString(getAptNum());
            addr2 += getAptChar();
        }

        String city = getLocation();
        String state = getState();
        String zip5 = getZip5();

        return new Address(addr1.trim(), addr2.trim(), city.trim(), state.trim(), zip5.trim(), "");
    }

    public boolean isEmpty()
    {
        return (this.street == null || this.street.isEmpty());
    }

    public String toString()
    {
        return this.toAddress().toString();
    }

    /** Getters / Setters */

    public int getBldgNum() {
        return bldgNum;
    }

    public void setBldgNum(int bldgNum) {
        this.bldgNum = bldgNum;
    }

    public String getStreet() {
        return (street != null) ? street : "";
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getBldgChar() {
        return (bldgChar != null) ? bldgChar : "";
    }

    public void setBldgChar(String bldgChar) {
        this.bldgChar = bldgChar;
    }

    public int getAptNum() {
        return aptNum;
    }

    public void setAptNum(int aptNum) {
        this.aptNum = aptNum;
    }

    public String getAptChar() {
        return (aptChar != null) ? aptChar : "";
    }

    public void setAptChar(String aptChar) {
        this.aptChar = aptChar;
    }

    public String getBldgParity() {
        return (bldgParity != null) ? bldgParity : "";
    }

    public void setBldgParity(String bldgParity) {
        this.bldgParity = bldgParity;
    }

    public String getAptParity() {
        return (aptParity != null) ? aptParity : "";
    }

    public void setAptParity(String aptParity) {
        this.aptParity = aptParity;
    }

    public String getPreDir() {
        return (preDir != null) ? preDir : "";
    }

    public void setPreDir(String preDir) {
        this.preDir = preDir;
    }

    public String getPostDir() {
        return (postDir != null) ? postDir : "";
    }

    public void setPostDir(String postDir) {
        this.postDir = postDir;
    }

    public String getInternal() {
        return (internal != null) ? internal : "";
    }

    public void setInternal(String internal) {
        this.internal = internal;
    }

    public String getStreetType() {
        return (streetType != null) ? streetType : "";
    }

    public void setStreetType(String streetType) {
        this.streetType = streetType;
    }

    public String getLocation() {
        return (location != null) ? location : "";
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getState() {
        return (state != null) ? state : "";
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setZip5(String zip5) {
        this.zip5 = zip5;
    }

    public String getZip5() {
        return (zip5 != null) ? zip5 : "";
    }
}

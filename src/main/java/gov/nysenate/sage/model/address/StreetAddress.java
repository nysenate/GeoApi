package gov.nysenate.sage.model.address;

/**
 * Represents a street address with address components broken down.
 */
public class StreetAddress
{
    protected int bldgNum;
    protected String bldgChar;
    protected String street;
    protected int aptNum;
    protected String aptChar;
    protected String preDir;
    protected String postDir;
    protected String internal;
    protected String streetType;
    protected String location;
    protected String state;
    protected String postal;

    public StreetAddress(){}

    /**
     * Converts the street address into a basic Address object performing the
     * necessary null checks.
     * @return  Address
     */
    public Address toAddress()
    {
        String addr1 = "";
        if (this.bldgNum != 0) addr1 += Integer.toString(this.bldgNum) + " ";
        if (this.preDir != null) addr1 += this.preDir + " ";
        if (this.street != null) addr1 += this.street + " ";
        if (this.streetType != null) addr1 += this.streetType + " ";
        if (this.postDir != null) addr1 += this.postDir + " ";

        String addr2 = "";
        if (this.internal != null && !this.internal.isEmpty()){
            addr2 = this.internal;
        }
        else if (this.bldgChar != null && !this.bldgChar.isEmpty()){
            addr2 = this.bldgChar;
        }
        else if (this.aptNum != 0) {
            addr2 += Integer.toString(this.aptNum);
            addr2 += (this.aptChar != null) ? this.aptChar : "";
        }

        String city = (this.location != null) ? location : "";
        String state = (this.state != null) ? this.state : "";
        String zip5 = (this.postal != null) ? postal : "";

        return new Address(addr1.trim(), addr2.trim(), city.trim(), state.trim(), zip5.trim(), null);
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
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getBldgChar() {
        return bldgChar;
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
        return aptChar;
    }

    public void setAptChar(String aptChar) {
        this.aptChar = aptChar;
    }

    public String getPreDir() {
        return preDir;
    }

    public void setPreDir(String preDir) {
        this.preDir = preDir;
    }

    public String getPostDir() {
        return postDir;
    }

    public void setPostDir(String postDir) {
        this.postDir = postDir;
    }

    public String getInternal() {
        return internal;
    }

    public void setInternal(String internal) {
        this.internal = internal;
    }

    public String getStreetType() {
        return streetType;
    }

    public void setStreetType(String streetType) {
        this.streetType = streetType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setPostal(String postal) {
        this.postal = postal;
    }

    public String getPostal() {
        return this.postal;
    }
}

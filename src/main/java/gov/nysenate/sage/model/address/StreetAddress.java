package gov.nysenate.sage.model.address;

/**
 * Represents a street address with address components broken down.
 */
public class StreetAddress
{
    protected int bldgNum;
    protected String bldgChar;
    protected String preDir;
    protected String street;
    protected String streetName;
    protected String streetType;
    protected String postDir;
    protected String internal;
    protected String location;
    protected String state;
    protected String zip5;
    protected String zip4;
    protected String poBox;
    protected boolean isHwy;
    protected String bldgParity;

    public StreetAddress(){}

    public StreetAddress(int bldgNum, String preDir, String streetName, String streetType, String postDir, String internal,
                         String location, String state, String zip5)
    {
        this.setBldgNum(bldgNum);
        this.setPreDir(preDir);
        this.setStreetName(streetName);
        this.setStreetType(streetType);
        this.setPostDir(postDir);
        this.setInternal(internal);
        this.setLocation(location);
        this.setState(state);
        this.setZip5(zip5);
    }

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
        if (!getPostDir().isEmpty()) addr1 += getPostDir() + " ";
        if (isPoBoxAddress()) addr1 += "PO Box: " + getPoBox();

        String addr2 = "";
        if (!getInternal().isEmpty()){
            addr2 = getInternal();
        }
        else if (!getBldgChar().isEmpty()){
            addr2 = getBldgChar();
        }

        String city = getLocation();
        String state = getState();
        String zip5 = getZip5();
        String zip4 = getZip4();

        return new Address(addr1.trim(), addr2.trim(), city.trim(), state.trim(), zip5.trim(), zip4.trim());
    }

    public boolean equals(StreetAddress streetAddress) {
        return (this.bldgNum == streetAddress.getBldgNum() &&
                this.preDir.equalsIgnoreCase(streetAddress.getPreDir()) &&
                this.postDir.equalsIgnoreCase(streetAddress.getPostDir()) &&
                this.streetName.equalsIgnoreCase(streetAddress.getStreetName()) &&
                this.streetType.equalsIgnoreCase(streetAddress.getStreetType()) &&
                this.location.equalsIgnoreCase(streetAddress.getLocation()) &&
                this.zip5.equalsIgnoreCase(streetAddress.getZip5()));
    }

    public boolean isStreetEmpty()
    {
        return this.getStreet().isEmpty();
    }

    public String toString()
    {
        return this.toAddress().toString();
    }

    public String toStringParsed()
    {
        return "bldgNum [" + this.getBldgNum() + "] preDir [" + this.getPreDir() + "] streetName [" + this.getStreetName() + "] postDir [" +
                this.getPostDir() + "] streetType [" + this.getStreetType() + "] city [" + this.getLocation() +
                "] state [" + this.getState() + "] zip5 [" + this.getZip5() + "]";
    }

    /** Getters / Setters */

    public int getBldgNum() {
        return bldgNum;
    }

    public void setBldgNum(int bldgNum) {
        this.bldgNum = bldgNum;
    }

    public String getStreet() {
        return (this.street != null) ? this.street :
               (!this.isHwy) ? this.getStreetName() + ((this.streetType != null) ? " " + this.streetType : "") :
                               this.getStreetType() + ((this.streetName != null) ? " " + this.streetName : "");
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

    public String getStreetName() {
        return (streetName != null && !streetName.equalsIgnoreCase("null")) ? streetName : "";
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getZip4() {
        return (zip4 != null) ? zip4 : "";
    }

    public void setZip4(String zip4) {
        this.zip4 = zip4;
    }

    public String getPoBox() {
        return (poBox != null) ? poBox : "";
    }

    public void setPoBox(String poBox) {
        this.poBox = poBox;
    }

    public boolean isPoBoxAddress() {
        return !getPoBox().isEmpty();
    }

    public boolean isHwy() {
        return isHwy;
    }

    public void setHwy(boolean hwy) {
        isHwy = hwy;
    }

    public String getBldgParity() {
        return (bldgParity != null) ? bldgParity : "";
    }

    public void setBldgParity(String bldgParity) {
        this.bldgParity = bldgParity;
    }
}

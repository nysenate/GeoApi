package gov.nysenate.sage.model.address;

public class StreetAddress extends Address
{
    protected int streetNumber;
    protected int bldgNumber;
    protected String bldgChar;
    protected int aptNumber;
    protected int aptChar;

    public StreetAddress(){}

    public int getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(int streetNumber) {
        this.streetNumber = streetNumber;
    }

    public int getBldgNumber() {
        return bldgNumber;
    }

    public void setBldgNumber(int bldgNumber) {
        this.bldgNumber = bldgNumber;
    }

    public String getBldgChar() {
        return bldgChar;
    }

    public void setBldgChar(String bldgChar) {
        this.bldgChar = bldgChar;
    }

    public int getAptNumber() {
        return aptNumber;
    }

    public void setAptNumber(int aptNumber) {
        this.aptNumber = aptNumber;
    }

    public int getAptChar() {
        return aptChar;
    }

    public void setAptChar(int aptChar) {
        this.aptChar = aptChar;
    }
}

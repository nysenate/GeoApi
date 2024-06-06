package gov.nysenate.sage.model.address;

/**
 * Represents a range of street addresses
 */
public class StreetAddressRange extends StreetAddress
{
    public int id;
    public int bldgLoNum;
    public int bldgHiNum;
    protected String bldgParity;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBldgLoNum() {
        return bldgLoNum;
    }

    public void setBldgLoNum(int bldgLoNum) {
        this.bldgLoNum = bldgLoNum;
    }

    public int getBldgHiNum() {
        return bldgHiNum;
    }

    public void setBldgHiNum(int bldgHiNum) {
        this.bldgHiNum = bldgHiNum;
    }

    public String getBldgParity() {
        return (bldgParity != null) ? bldgParity : "";
    }

    public void setBldgParity(String bldgParity) {
        this.bldgParity = bldgParity;
    }
}

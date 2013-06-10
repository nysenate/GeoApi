package gov.nysenate.sage.model.address;

/**
 * Represents a range of street addresses
 */
public class StreetAddressRange extends StreetAddress
{
    public int id;
    public int bldgLoNum;
    public String bldgLoChr;
    public int bldgHiNum;
    public String bldgHiChr;

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
}

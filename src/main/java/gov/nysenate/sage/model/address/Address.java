package gov.nysenate.sage.model.address;

import java.io.Serializable;

/**
 * A generic address structure for representing the basic address components.
 * Can be utilized for address validation or to serve as a base for more detailed
 * address component classes. The fields in this class should never be null but
 * rather an empty string if unassigned.
 *
 * @author Graylin Kim, Ash Islam
 */
public class Address implements Serializable, Cloneable
{
    /** Basic address components */
    protected String addr1 = "";
    protected String addr2 = "";
    protected String city = "";
    protected String state = "";
    protected String zip5 = "";
    protected String zip4 = "";

    public Address() {}

    public Address(String addr1)
    {
        this(addr1, "","","","","");
    }

    public Address(String addr1, String addr2, String city, String state, String zip5, String zip4)
    {
        this.setAddr1(addr1);
        this.setAddr2(addr2);
        this.setCity(city);
        this.setState(state);
        this.setZip5(zip5);
        this.setZip4(zip4);
    }

    public String getAddr1() {
        return addr1;
    }

    public void setAddr1(String addr1) {
        if (addr1 != null ){
            this.addr1 = addr1;
        }
    }

    public String getAddr2() {
        return addr2;
    }

    public void setAddr2(String addr2) {
        if (addr2 != null){
            this.addr2 = addr2;
        }
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        if (city != null) {
            this.city = city;
        }
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        if (state != null){
            this.state = state;
        }
    }

    public String getZip5() {
        return zip5;
    }

    public void setZip5(String zip5) {
        if (zip5 != null){
            this.zip5 = zip5;
        }
    }

    public String getZip4() {
        return zip4;
    }

    public void setZip4(String zip4) {
        if (zip4 != null){
            this.zip4 = zip4;
        }
    }

    @Override
    public Address clone()
    {
        try {
            return (Address)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            return null;
        }
    }

    public boolean isParsed()
    {
        return !(addr2.equals("") && city.equals("") && state.equals("") && zip5.equals(""));
    }

    @Override
    public String toString()
    {
        if (isParsed())
        {
            return ( addr1.equals("") ? addr1+" " : "") + ( !addr2.equals("") ? addr2+" " : "")
                    + ( !city.equals("")  ? ", "+city+" "   : "") + ( !state.equals("") ? state+" " : "")
                    + ( !zip5.equals("")  ? zip5 : "") + ( !zip4.equals("")  ? "-"+zip4 : "").trim();
        }
        else
        {
            return addr1;
        }
    }
}
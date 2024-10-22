package gov.nysenate.sage.model.address;

import gov.nysenate.sage.util.FormatUtil;

import java.io.Serializable;
import java.util.List;

/**
 * A generic address structure for representing the basic address components.
 * Can be utilized for address validation or to serve as a base for more detailed
 * address component classes. The fields in this class should never be null but
 * rather an empty string if unassigned.
 *
 * @author Graylin Kim, Ash Islam
 */
public class Address implements Serializable, Cloneable {
    // TODO: what if rev-geocode as non-NY address?
    private static final String poBoxPattern = "(?i)PO Box \\d+";
    // Note that these can never be null, since cleanString never returns null on non-null input.
    private String addr1 = "";
    private String addr2 = "";
    private String city = "";
    private Zip5 zip5 = null;
    private Zip4 zip4 = null;
    // ID is only used for batch districting requests
    private Integer id = null;

    /** Verification info */
    private boolean uspsValidated = false;

    public Address() {}

    public Address(String addr1) {
        this.addr1 = addr1;
    }

    public Address(String addr1, String postalCity, String state, String postal) {
        setAddr1(addr1);
        setPostalCity(postalCity);
        setZip9(postal);
    }

    public Address(String addr1, String postalCity, String state, String postal, Integer id) {
        setAddr1(addr1);
        setPostalCity(postalCity);
        setZip9(postal);
        setId(id);
    }

    public Address(String addr1, String addr2, String postalCity, String state, String zip5, String zip4) {
        setAddr1(addr1);
        setAddr2(addr2);
        setPostalCity(postalCity);
        setZip5(Integer.valueOf(zip5));
        setZip4(Integer.valueOf(zip4));
    }

    public boolean isEmpty() {
        return addr1.trim().isEmpty();
    }

    @Override
    public String toString() {
        return ((!addr1.isEmpty() ? addr1 : "") + (!addr2.isEmpty() ? " " + addr2 : "")
                + (!addr1.isEmpty() || !addr2.isEmpty() ? "," : "")
                + (!city.isEmpty() ? " " + city + "," : "")
                + (!zip5.isMissing() ? " " + zip5 : "") + (!zip4.isMissing() ? "-" + zip4 : "")).trim();
    }

    public String toLogString() {
        return "addr1=" + addr1 +"&addr2=" + addr2 + "&city=" + city +
                "&zip5=" + zip5 + "&zip4=" + zip4;
    }

    /**
     * Normalization applied:
     * - Remove the dash within the building number
     * @return String
     */
    public String toNormalizedString() {
        return toString().replaceFirst("^(\\d+)(-)(\\d+)","$1$3");
    }

    public String getAddr1() {
        return addr1;
    }

    public void setAddr1(String addr1) {
        if (addr1 != null){
            this.addr1 = FormatUtil.cleanString( addr1 );
        }
    }

    public String getAddr2() {
        return addr2;
    }

    public void setAddr2(String addr2) {
        if (addr2 != null){
            this.addr2 = FormatUtil.cleanString( addr2 );
        }
    }

    public String getPostalCity() {
        return city;
    }

    public void setPostalCity(String postalCity) {
        if (postalCity != null) {
            postalCity = postalCity.replaceFirst("^(TOWN|CITY) (OF )?", "")
                    .replaceFirst("(\\(CITY\\)|/CITY)$", "");
            this.city = FormatUtil.cleanString(postalCity);
        }
    }

    public String getState() {
        return "NY";
    }

    public Integer getZip5() {
        return zip5.zip();
    }

    public void setZip5(Integer zip5) {
        this.zip5 = new Zip5(zip5);
    }

    public Integer getZip4() {
        return zip4.zip();
    }

    public void setZip4(Integer zip4) {
        this.zip4 = new Zip4(zip4);
    }

    /** Stores 12345-1234 style postal codes into zip5 and zip4 parts */
    public void setZip9(String postal) {
        if (postal != null) {
            List<String> zipParts = List.of(postal.split("-"));
            if (!zipParts.isEmpty()) {
                setZip5(Integer.parseInt(zipParts.get(0).trim()));
            }
            if (zipParts.size() > 1) {
                setZip4(Integer.parseInt(zipParts.get(1).trim()));
            }
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /** Indicates if address has been marked USPS validated. */
    public boolean isUspsValidated() {
        return uspsValidated;
    }

    /** Marks address as validated by USPS. */
    public void setUspsValidated(boolean uspsValidated) {
        this.uspsValidated = uspsValidated;
    }

    /** Address is eligible for usps validation if addr1 and either zip or city/state are set. */
    public boolean isEligibleForUSPS() {
        return !addr1.isEmpty() && (zip5.zip() != null || !city.isEmpty());
    }

    public boolean isPOBox() {
        return addr1.replaceAll("[.,:]", "")
                .replaceAll("\\s+", " ").matches(poBoxPattern);
    }

    @Override
    public Address clone() {
        try {
            return (Address)super.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public static boolean validState(String state) {
        return state.replaceAll("[.]", "").toUpperCase().trim().matches("^$|NY|NEW YORK");
    }
}

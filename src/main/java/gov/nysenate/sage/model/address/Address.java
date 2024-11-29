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
public class Address implements Serializable {
    private static final String poBoxPattern = "(?i)PO Box \\d+";
    private String streetWithNum;
    private String postalCity;
    private String state;
    private Zip5 zip5;
    private Zip4 zip4;
    private String internal;

    /** Verification info */
    private boolean uspsValidated = false;

    public Address() {}

    public Address(String fullAddr) {
        // TODO: complete. Perhaps get a full list of street, town, zip5, zip4?
    }

    public Address(String streetWithNum, String postalCity, String zip5) {
        this(streetWithNum, postalCity, "NY", zip5);
    }

    public Address(String addr1, String postalCity, String state, String postal) {
        this.streetWithNum = addr1;
        setPostalCity(postalCity);
        this.state = state;
        setZip9(postal);
    }

    public Address(String addr1, String addr2, String postalCity, String state, String zip5, String zip4) {
        this.streetWithNum = addr1;
        this.internal = addr2;
        setPostalCity(postalCity);
        this.state = state;
        setZip9(zip5 + "-" + zip4);
    }

    public String getStreetWithNum() {
        return streetWithNum;
    }

    public void setStreetWithNum(String streetWithNum) {
        this.streetWithNum = streetWithNum;
    }

    public String getPostalCity() {
        return postalCity;
    }

    public Integer getZip5() {
        return zip5.zip();
    }

    public void setZip5(String zip5) {
        this.zip5 = new Zip5(Integer.parseInt(zip5.trim()));
    }

    public Integer getZip4() {
        return zip4.zip();
    }

    public void setZip4(String zip4) {
        this.zip4 = new Zip4(Integer.parseInt(zip4.trim()));
    }

    @Override
    public String toString() {
        return ((!streetWithNum.isEmpty() ? streetWithNum : "") + (!internal.isEmpty() ? " " + internal : "")
                + (!streetWithNum.isEmpty() || !internal.isEmpty() ? "," : "")
                + (!postalCity.isEmpty() ? " " + postalCity + "," : "")
                + (!zip5.isMissing() ? " " + zip5 : "") + (!zip4.isMissing() ? "-" + zip4 : "")).trim();
    }

    /**
     * Normalization applied:
     * - Remove the dash within the building number
     * @return String
     */
    public String toNormalizedString() {
        return toString().replaceFirst("^(\\d+)(-)(\\d+)","$1$3");
    }

    public String getInternal() {
        return internal;
    }

    public void setInternal(String addr2) {
        if (addr2 != null) {
            this.internal = FormatUtil.cleanString(addr2);
        }
    }

    public void setPostalCity(String postalCity) {
        if (postalCity != null) {
            postalCity = postalCity.replaceFirst("^(TOWN|CITY) (OF )?", "")
                    .replaceFirst("(\\(CITY\\)|/CITY)$", "");
            this.postalCity = FormatUtil.cleanString(postalCity);
        }
    }

    public String getState() {
        // TODO: some enforcement of NY only addresses
        return state;
    }

    /** Stores 12345-1234 style postal codes into zip5 and zip4 parts */
    public void setZip9(String postal) {
        if (postal != null) {
            List<String> zipParts = List.of(postal.split("-"));
            if (!zipParts.isEmpty()) {
                this.zip5 = new Zip5(Integer.parseInt(zipParts.get(0).trim()));
            }
            if (zipParts.size() > 1) {
                this.zip5 = new Zip5(Integer.parseInt(zipParts.get(1).trim()));
            }
        }
    }

    /** Indicates if address has been marked USPS validated. */
    public boolean isUspsValidated() {
        return uspsValidated;
    }

    /** Marks address as validated by USPS. */
    public void setUspsValidated(boolean uspsValidated) {
        this.uspsValidated = uspsValidated;
    }

    public boolean isValid() {
        return !streetWithNum.isEmpty() && (!postalCity.isEmpty() || !zip5.isMissing());
    }

    public boolean isPOBox() {
        return streetWithNum.replaceAll("[.,:]", "")
                .replaceAll("\\s+", " ").matches(poBoxPattern);
    }

    public static boolean validState(String state) {
        return state.replaceAll("[.]", "").toUpperCase().trim().matches("^$|NY|NEW YORK");
    }
}

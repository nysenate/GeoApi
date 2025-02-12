package gov.nysenate.sage.model.address;

import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.util.FormatUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract sealed class Address permits BuildingAddress, PostOfficeBox {
    private static final Pattern poBoxPattern = Pattern.compile("(?i)PO Box (\\d+)");
    private String postalCity;
    private String state = "NY";
    private Zip5 zip5;
    private Zip4 zip4 = new Zip4(null);

    /** Verification info */
    private boolean uspsValidated = false;

    public static Address getAddress(String addr) {
        // TODO: complete. Perhaps get a full list of street, town, zip5, zip4?
        return null;
    }

    public static Address getAddress(String addr1, String addr2, String city, String state, String zip5, String zip4) {
        Matcher poBoxMatcher =  poBoxPattern.matcher(addr1);
        if (poBoxMatcher.matches()) {
            int boxNumber = Integer.parseInt(poBoxMatcher.group(1));
            return new PostOfficeBox(boxNumber, addr2, city, state, zip5, zip4);
        }
        return new BuildingAddress(addr1, addr2, city, state, zip5, zip4);
    }

    public Address(AddressWithoutNum awn) {
        this.postalCity = awn.postalCity();
        this.zip5 = new Zip5(awn.zip5());
    }

    public Address(String postalCity, String state, String postal) {
        setPostalCity(postalCity);
        this.state = state;
        setZip9(postal);
    }

    public abstract String getAddr1();

    public String getAddr2() {
        return "";
    }

    public String getPostalCity() {
        return postalCity;
    }

    public Zip5 getZip5() {
        return zip5;
    }

    public Zip4 getZip4() {
        return zip4;
    }

    public void setZip4(String zip4) {
        this.zip4 = new Zip4(zip4 == null ? null : Integer.parseInt(zip4.trim()));
    }

    @Override
    public String toString() {
        return (!postalCity.isEmpty() ? " " + postalCity + "," : "")
                + (!zip5.isMissing() ? " " + zip5 : "") + (!zip4.isMissing() ? "-" + zip4 : "");
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
        return !postalCity.isEmpty() || !zip5.isMissing();
    }

    public static boolean validState(String state) {
        return state.replaceAll("[.]", "").toUpperCase().trim().matches("^$|NY|NEW YORK");
    }

    // TODO: can switch on sealed classes in Java 21
    public boolean isPoBox() {
        return false;
    }
}

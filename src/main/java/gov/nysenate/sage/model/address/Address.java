package gov.nysenate.sage.model.address;

import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract sealed class Address permits BuildingAddress, PostOfficeBox {
    private static final Pattern poBoxPattern = Pattern.compile("(?i)PO Box (\\d+)");
    private String postalCity;
    private String state = "NY";
    private final Zip5 zip5;
    private Zip4 zip4 = new Zip4(null);

    /** Verification info */
    private boolean uspsValidated = false;

    public static Address getAddress(String addr) {
        String[] csv = addr.split(" *, *");
        String zip4 = null;
        if (csv.length == 5) {
            zip4 = csv[4];
        }
        else if (csv.length == 4) {
            String[] splitZip = csv[3].split("-");
            if (splitZip.length > 1) {
                csv[3] = splitZip[0];
                zip4 = splitZip[1];
            }
        }
        else {
            throw new IllegalArgumentException("Invalid address: " + addr);
        }
        return getAddress(csv[0], "", csv[1], csv[2], csv[3], zip4);
    }

    public static Address getAddress(String addr1, String addr2, String city, String state, String zip5, String zip4) {
        Matcher poBoxMatcher = poBoxPattern.matcher(addr1);
        if (poBoxMatcher.matches()) {
            int boxNumber = Integer.parseInt(poBoxMatcher.group(1));
            return new PostOfficeBox(boxNumber, addr2, city, state, zip5, zip4);
        }
        var tempBldgAddr = new BuildingAddress(addr1, city, state, zip5, zip4);
        tempBldgAddr.setInternal(addr2);
        return tempBldgAddr;
    }

    public Address(AddressWithoutNum awn) {
        this.postalCity = awn.postalCity();
        this.zip5 = new Zip5(awn.zip5());
    }

    public Address(String postalCity, String state, String zip5, String zip4) {
        setPostalCity(postalCity);
        this.state = state;
        this.zip5 = new Zip5(zip5.trim());
        this.zip4 = new Zip4(zip4.trim());
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
        this.zip4 = new Zip4(zip4.trim());
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

    /** Indicates if address has been marked USPS validated. */
    public boolean isUspsValidated() {
        return uspsValidated;
    }

    /** Marks address as validated by USPS. */
    public void setUspsValidated(boolean uspsValidated) {
        this.uspsValidated = uspsValidated;
    }

    public boolean isValid() {
        return !StringUtils.isBlank(postalCity) || !zip5.isMissing();
    }

    public static boolean validState(String state) {
        return state.replaceAll("[.]", "").toUpperCase().trim().matches("^$|NY|NEW YORK");
    }

    // TODO: can switch on sealed classes in Java 21
    public boolean isPoBox() {
        return false;
    }
}

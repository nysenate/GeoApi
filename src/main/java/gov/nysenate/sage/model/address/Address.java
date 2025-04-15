package gov.nysenate.sage.model.address;

import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract sealed class Address permits BuildingAddress, PostOfficeBox, UnparsedAddress {
    private static final Pattern poBoxPattern = Pattern.compile("(?i)PO Box (\\d+)"),
            zipPattern = Pattern.compile("(\\d{5})(-\\d{4})?");
    private String postalCity;
    private String state = "NY";
    private Zip5 zip5;
    private Zip4 zip4;

    /** Verification info */
    private boolean uspsValidated = false;

    public static Address getAddress(String addr) {
        String[] csv = addr.split(" *, *");
        if (csv.length != 3 && csv.length != 4) {
            throw new IllegalArgumentException("Invalid address: " + addr);
        }
        String state = csv[2];
        String zip5 = null, zip4 = null;
        Matcher zipMatcher = zipPattern.matcher(csv[2]);
        if (zipMatcher.find()) {
            zip5 = zipMatcher.group(1);
            zip4 = zipMatcher.group(2);
            state = state.replaceFirst(zipMatcher.group(), "").trim();
        }
        return getAddress(csv[0], "", csv[1], state, zip5, zip4);
    }

    public static Address getAddress(String addr1, String addr2, String city, String state, String zip5, String zip4) {
        try {
            Matcher poBoxMatcher = poBoxPattern.matcher(addr1);
            if (poBoxMatcher.matches()) {
                int boxNumber = Integer.parseInt(poBoxMatcher.group(1));
                return new PostOfficeBox(boxNumber, addr2, city, state, zip5, zip4);
            }
            var tempBldgAddr = new BuildingAddress(addr1, city, state, zip5, zip4);
            tempBldgAddr.setInternal(addr2);
            return tempBldgAddr;
        } catch (Exception ex) {
            return new UnparsedAddress(addr1, city, state, zip5, zip4);
        }
    }

    protected Address(AddressWithoutNum awn) {
        this.postalCity = awn.postalCity();
        this.zip5 = awn.zip5();
    }

    protected Address(String postalCity, String state, String zip5, String zip4) {
        setPostalCity(postalCity);
        this.state = state;
        if (!StringUtils.isBlank(zip5)) {
            this.zip5 = new Zip5(zip5);
        }
        if (!StringUtils.isBlank(zip4)) {
            this.zip4 = new Zip4(zip4);
        }
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

    @Override
    public String toString() {
        return (postalCity.isEmpty() ? "" : " " + postalCity) + (state.isEmpty() ? "" : ", " + state)
                + (zip5 == null ? "" : ", " + zip5) + (zip4 == null ? "" : "-" + zip4);
    }

    public void setPostalCity(String postalCity) {
        if (postalCity != null) {
            postalCity = postalCity.replaceFirst("^(TOWN|CITY) (OF )?", "")
                    .replaceFirst("(\\(CITY\\)|/CITY)$", "");
            this.postalCity = FormatUtil.cleanString(postalCity);
        }
    }

    public String getState() {
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
        return !StringUtils.isBlank(postalCity) || zip5 != null;
    }

    public boolean isOutOfState() {
        return state != null && !state.replaceAll("[.]", "").toUpperCase().trim().matches("^$|NY|NEW YORK");
    }
}

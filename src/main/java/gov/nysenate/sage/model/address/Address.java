package gov.nysenate.sage.model.address;

import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public sealed class Address permits BuildingAddress, PostOfficeBox {
    private static final Pattern zipPattern = Pattern.compile("(\\d{5})(-\\d{4})?");
    private String addr1, addr2;
    private String postalCity;
    private String state = "NY";
    private Zip5 zip5;
    private Zip4 zip4;

    public static Address getAddress(String addr) {
        String postalCity = null, state = null, zip5 = null, zip4 = null;
        String[] csv = addr.split(" *, *");
        for (int i = 1; i < csv.length; i++) {
            String part = csv[i].trim();
            if (part.length() == 2) {
                state = part;
            }
            else {
                Matcher zipMatcher = zipPattern.matcher(part);
                if (zipMatcher.matches()) {
                    String[] zips = part.split("-");
                    zip5 = zips[0];
                    if (zips.length == 2) {
                        zip4 = zips[1];
                    }
                }
                else if (postalCity == null) {
                    postalCity = part;
                }
            }
        }
        return new Address(csv[0], "", postalCity, state, zip5, zip4);
    }

    public Address(String addr1, String addr2, String postalCity, String state, String zip5, String zip4) {
        this.addr1 = addr1;
        this.addr2 = addr2;
        this.postalCity = postalCity;
        this.state = state;
        this.zip5 = new Zip5(zip5);
        this.zip4 = new Zip4(zip4);
    }

    protected Address(Address addrToCopy) {
        this.addr1 = addrToCopy.getAddr1();
        this.addr2 = addrToCopy.getAddr2();
        this.postalCity = addrToCopy.getPostalCity();
        this.state = addrToCopy.getState();
        this.zip5 = addrToCopy.getZip5();
        this.zip4 = addrToCopy.getZip4();
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

    public String getAddr1() {
        return addr1;
    }

    public String getAddr2() {
        return addr2;
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
        return addr1 + " " + (StringUtils.isBlank(postalCity) ? "" : postalCity) + (StringUtils.isBlank(state) ? "" : ", " + state)
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
        return false;
    }

    public boolean isValid() {
        return !StringUtils.isBlank(postalCity) || zip5 != null;
    }

    public boolean isOutOfState() {
        return state != null && !state.replaceAll("[.]", "").toUpperCase().trim().matches("^$|NY|NEW YORK");
    }
}

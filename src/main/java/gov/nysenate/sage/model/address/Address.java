package gov.nysenate.sage.model.address;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public sealed class Address permits BuildingAddress, PostOfficeBox {
    private static final Pattern zipPattern = Pattern.compile("(\\d{5})(-\\d{4})?");
    private final String addr1, addr2, postalCity,
    // TODO: Should maybe default to NY/exclude it as a parameter
            state;
    private final Zip5 zip5;
    private final Zip4 zip4;

    public Address(String addr1, String addr2, String postalCity,
                   String state, String zip5, String zip4) {
        this(addr1, addr2, postalCity, state,
                StringUtils.isBlank(zip5) ? null : new Zip5(zip5), StringUtils.isBlank(zip4) ? null : new Zip4(zip4));
    }

    public Address(String addr1, String postalCity, String state, String zip5) {
        this(addr1, null, postalCity, state, zip5, null);
    }

    protected Address(Address addrToCopy) {
        this(addrToCopy.addr1, addrToCopy.addr2, addrToCopy.postalCity,
                addrToCopy.state, addrToCopy.zip5, addrToCopy.zip4);
    }

    private Address(String addr1, String addr2, String postalCity, String state, Zip5 zip5, Zip4 zip4) {
        this.addr1 = addr1;
        this.addr2 = addr2;
        // "The Bronx" is sometimes used, but "Bronx" is official and validates properly.
        if (postalCity != null) {
            postalCity = postalCity.replaceAll("(?i)\\bThe Bronx", "Bronx");
        }
        this.postalCity = postalCity;
        this.state = state;
        this.zip5 = zip5;
        this.zip4 = zip4;
    }

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

    public String getPrimaryAddr1() {
        return addr1;
    }

    @Override
    public String toString() {
        String ret = Stream.of(addr1, postalCity, state, zip5)
                .filter(field -> field != null && field.toString().isBlank())
                .map(Object::toString).collect(Collectors.joining(", "));
        if (zip4 != null) {
            ret += "-" + zip4;
        }
        return ret;
    }

    /** Indicates if address has been marked USPS validated. */
    public boolean isUspsValidated() {
        return false;
    }

    public boolean isValid() {
        return !StringUtils.isBlank(addr1) && (!StringUtils.isBlank(postalCity) || zip5 != null);
    }

    public boolean isCacheable() {
        return !StringUtils.isBlank(addr1) && postalCity != null && zip5 != null && state != null;
    }

    public boolean isOutOfState() {
        return state != null && !state.replaceAll("[.]", "").toUpperCase().trim().matches("^$|NY|NEW YORK");
    }
}

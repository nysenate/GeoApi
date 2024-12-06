package gov.nysenate.sage.model.address;

import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 *
 */
public class Address {
    private static final String bldgNumPattern = "^[0-9]+-?[0-9]*[a-zA-Z]?";
    private static final String poBoxPattern = "(?i)PO Box \\d+";
    private String bldgId;
    private String street;
    private String postalCity;
    private String state = "NY";
    private Zip5 zip5;
    private Zip4 zip4 = new Zip4(null);
    private String internal;

    /** Verification info */
    private boolean uspsValidated = false;

    public Address() {}

    public Address(String fullAddr) {
        // TODO: complete. Perhaps get a full list of street, town, zip5, zip4?
    }

    public Address(int bldgNum, AddressWithoutNum awn) {
        this.bldgId = String.valueOf(bldgNum);
        this.street = awn.street();
        this.postalCity = awn.postalCity();
        this.zip5 = new Zip5(awn.zip5());
    }

    public Address(String streetWithNum, String postalCity, String zip5) {
        this(streetWithNum, postalCity, "NY", zip5);
    }

    public Address(String addr1, String postalCity, String state, String postal) {
        setStreetWithNum(addr1);
        setPostalCity(postalCity);
        this.state = state;
        setZip9(postal);
    }

    public Address(String addr1, String addr2, String postalCity, String state, String zip5, String zip4) {
        this(addr1, postalCity, state, zip5 + "-" + zip4);
        this.internal = addr2;
    }

    public static Address getAddress(String bldgId, String street, String postalCity, String zip5, String zip4) {
        var addr = new Address();
        addr.bldgId = bldgId;
        addr.street = street;
        addr.postalCity = postalCity;
        addr.setZip5(zip5);
        addr.setZip4(zip4);
        return addr;
    }

    public String getBldgId() {
        return bldgId;
    }

    public String getStreet() {
        return street;
    }

    public String getStreetWithNum() {
        return bldgId + " " + street;
    }

    public void setStreetWithNum(String streetWithNum) {
        Pair<String> parts = splitBldgId(streetWithNum);
        this.bldgId = parts.first();
        // The following line would remove all numerical suffixes and special characters.
        // This causes problems when matching the street file table. This may adversely affect the geocache table
        this.street = parts.second().replaceAll("[#:;.,']", "").replaceAll("[ -]+", " ").toUpperCase();
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
        this.zip4 = new Zip4(zip4 == null ? null : Integer.parseInt(zip4.trim()));
    }

    @Override
    public String toString() {
        return bldgId + " " + street + (!internal.isEmpty() ? " " + internal : "")
                + (!postalCity.isEmpty() ? " " + postalCity + "," : "")
                + (!zip5.isMissing() ? " " + zip5 : "") + (!zip4.isMissing() ? "-" + zip4 : "");
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
        return !StringUtils.isBlank(bldgId) && !street.isEmpty() &&
                (!postalCity.isEmpty() || !zip5.isMissing());
    }

    public boolean isPOBox() {
        // TODO: another subclass
//        return streetWithNum.replaceAll("[.,:]", "")
//                .replaceAll("\\s+", " ").matches(poBoxPattern);
        return false;
    }

    public static boolean validState(String state) {
        return state.replaceAll("[.]", "").toUpperCase().trim().matches("^$|NY|NEW YORK");
    }

    private static Pair<String> splitBldgId(String toSplit) {
        // TODO: may need to remove "#"
        String[] parts = toSplit.trim().split(" ", 2);
        if (parts.length != 2 || !parts[0].matches(bldgNumPattern)) {
            throw new IllegalArgumentException("Cannot parse bldg ID from: " + toSplit);
        }
        return new Pair<>(parts[0], parts[1]);
    }
}

package gov.nysenate.sage.scripts.streetfinder.parsers;

public enum NonStandardAddressType {
    NORMAL(true), NON_STANDARD_APT_TYPE(true), MULTIPLE_APT_TYPES(true),
    COLLEGE(false), NO_STREET_NUM(false), MULTIPLE_ADDRESSES(false), OTHER(false);

    private final boolean valid;

    NonStandardAddressType(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }
}

package gov.nysenate.sage.model.address;

public final class UnparsedAddress extends Address {
    private final String addr1;
    public UnparsedAddress(String addr1, String postalCity, String state, String zip5, String zip4) {
        super(postalCity, state, zip5, zip4);
        this.addr1 = addr1;
    }

    @Override
    public String getAddr1() {
        return addr1;
    }

    @Override
    public boolean isValid() {
        return false;
    }
}

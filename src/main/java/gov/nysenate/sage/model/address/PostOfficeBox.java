package gov.nysenate.sage.model.address;

public final class PostOfficeBox extends Address {
    private final int boxNumber;

    public PostOfficeBox(int boxNumber, String addr2, String postalCity, String state, String zip5, String zip4) {
        super(postalCity, state, zip5, zip4);
        this.boxNumber = boxNumber;
    }

    @Override
    public String getAddr1() {
        return "PO BOX " + boxNumber;
    }

    @Override
    public boolean isPoBox() {
        return true;
    }
}

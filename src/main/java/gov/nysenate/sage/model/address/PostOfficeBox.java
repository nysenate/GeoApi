package gov.nysenate.sage.model.address;

public final class PostOfficeBox extends Address {
    private final int boxNumber;

    // TODO: what is "street" here?
    public PostOfficeBox(int boxNumber, String street, String postalCity, String state, String zip5, String zip4) {
        super(street, postalCity, zip5 + (zip4.isEmpty() ? "" : "-" + zip4));
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

package gov.nysenate.sage.model.address;

public final class PostOfficeBox extends Address {
    // We actually have no use to add more fields: we just need to know it's a PO box.
    public PostOfficeBox(Address baseAddress) {
        super(baseAddress);
    }

    @Override
    public boolean isUspsValidated() {
        return true;
    }
}

package gov.nysenate.sage.model.address;

public final class Zip4 extends Zip {
    public Zip4(String zip) {
        super(zip);
    }

    @Override
    protected int numDigits() {
        return 4;
    }
}

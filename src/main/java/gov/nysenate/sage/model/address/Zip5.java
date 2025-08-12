package gov.nysenate.sage.model.address;

public final class Zip5 extends Zip {
    public Zip5(int zip) {
        super(zip);
    }

    public Zip5(String zip) {
        super(zip);
    }

    @Override
    protected int numDigits() {
        return 5;
    }
}

package gov.nysenate.sage.model.address;

public abstract class Zip {
    private final Integer zip;

    public Zip(Integer zip, int maxDigits) {
        if (zip != null && (zip <= 0 || zip >= Math.pow(10, maxDigits))) {
            throw new IllegalArgumentException("Zip number must be positive with at most " + maxDigits + " digits");
        }
        this.zip = zip;
    }

    @Override
    public String toString() {
        return zip == null ? null : zip.toString();
    }

    public Integer zip() {
        return zip;
    }

    public boolean isMissing() {
        return zip == null;
    }
}

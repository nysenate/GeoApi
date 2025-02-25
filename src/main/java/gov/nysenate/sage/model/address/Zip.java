package gov.nysenate.sage.model.address;

import org.apache.commons.lang3.StringUtils;

public abstract class Zip {
    private final Integer zip;

    public Zip(Integer zip) {
        if (zip != null && (zip <= 0 || zip >= Math.pow(10, numDigits()))) {
            throw new IllegalArgumentException("Zip number must be positive with at most " + numDigits() + " digits");
        }
        this.zip = zip;
    }

    public Zip(String zip) {
        this(StringUtils.isBlank(zip) ? null : Integer.parseInt(zip.trim()));
    }

    public Integer zip() {
        return zip;
    }

    protected abstract int numDigits();

    public boolean isMissing() {
        return zip == null;
    }

    @Override
    public String toString() {
        var tempString = new StringBuilder(String.valueOf(zip));
        while (tempString.length() < numDigits()) {
            tempString.insert(0, "0");
        }
        return tempString.toString();
    }
}

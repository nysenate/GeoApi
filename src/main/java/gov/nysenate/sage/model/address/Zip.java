package gov.nysenate.sage.model.address;

public sealed abstract class Zip permits Zip5, Zip4 {
    private final int zip;

    public Zip(int zip) {
        if (zip <= 0 || zip >= Math.pow(10, numDigits())) {
            throw new IllegalArgumentException(zip + " is not a valid zip" + numDigits());
        }
        this.zip = zip;
    }

    public Zip(String zip) {
        this(Integer.parseInt(zip.trim()));
    }

    protected abstract int numDigits();

    @Override
    public int hashCode() {
        return zip;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return zip == ((Zip) obj).zip;
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

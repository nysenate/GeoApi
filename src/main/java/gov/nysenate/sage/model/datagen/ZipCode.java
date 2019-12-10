package gov.nysenate.sage.model.datagen;

public class ZipCode {

    private String zipcode;

    public ZipCode() {}

    public ZipCode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String toString() { return zipcode; }
}


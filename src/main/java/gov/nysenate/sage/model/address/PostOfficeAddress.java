package gov.nysenate.sage.model.address;

/**
 * Contains all Post Office address data we care about.
 */
public record PostOfficeAddress(int deliveryZip, String streetWithNum, String city, int zip5, int zip4) {
    public PostOfficeAddress(String deliveryZip, String streetWithNum, String city, String zip5, String zip4) {
        this(Integer.parseInt(deliveryZip), streetWithNum, city, Integer.parseInt(zip5), Integer.parseInt(zip4));
    }

    public Address fullAddress() {
        var result = new Address(streetWithNum);
        result.setCity(city);
        result.setZip5(String.valueOf(zip5));
        result.setZip4(String.valueOf(zip4));
        return result;
    }
}

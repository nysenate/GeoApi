package gov.nysenate.sage.model.address;

/**
 * Contains all Post Office address data we care about.
 */
public record PostOfficeAddress(int deliveryZip, Address address) {
    public PostOfficeAddress(String deliveryZip, Address address) {
        this(Integer.parseInt(deliveryZip), address);
    }
}

package gov.nysenate.sage.model.address;

/**
 * Contains all Post Office address data we care about.
 */
public class PostOfficeAddress {
    private final int deliveryZip;
    private Address address;

    public PostOfficeAddress(int deliveryZip, Address address) {
        this.deliveryZip = deliveryZip;
        this.address = address;
    }

    public int getDeliveryZip() {
        return deliveryZip;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}

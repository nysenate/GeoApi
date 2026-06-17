package gov.nysenate.sage.model.district;

import gov.nysenate.sage.client.view.address.AddressView;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.services.model.Office;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OfficeInfo {
    private AddressView address;
    private String name;
    private String phone;
    private Point point = null;

    public OfficeInfo(Office senateOffice) {
        String addr1 = senateOffice.getStreet();
        String[] zips = senateOffice.getPostalCode().split("-");
        this.address = new AddressView(new Address(addr1, senateOffice.getAdditional(), senateOffice.getCity(), "NY",
                zips[0], zips.length > 1 ? zips[1] : null), false);
        this.name = senateOffice.getName();
        this.phone = senateOffice.getPhone();
    }
}

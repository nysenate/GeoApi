package gov.nysenate.sage.model.district;

import gov.nysenate.Office;
import gov.nysenate.sage.client.view.address.AddressView;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.geo.Point;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
public class OfficeInfo {
    private AddressView address;
    private String name;
    private String phone;
    private Point point = null;

    public OfficeInfo(Office senateOffice) {
        String addr1 = senateOffice.street();
        String[] zips = senateOffice.postalCode().split("-");
        this.address = new AddressView(new Address(addr1, senateOffice.additional(), senateOffice.city(), "NY",
                zips[0], zips.length > 1 ? zips[1] : null), false);
        this.name = StringUtils.trimToNull(senateOffice.name());
        this.phone = StringUtils.trimToNull(senateOffice.phone());
    }
}

package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.model.address.PostOfficeAddress;

import javax.annotation.Nonnull;
import java.util.List;

public interface PostOfficeDao {
    @Nonnull
    List<PostOfficeAddress> getPostOffices(int deliveryZip);

    /**
     * Clears the database table, and adds the given data.
     */
    void replaceData(List<PostOfficeAddress> postalAddresses);
}

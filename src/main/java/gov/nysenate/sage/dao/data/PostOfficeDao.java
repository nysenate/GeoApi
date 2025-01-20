package gov.nysenate.sage.dao.data;

import com.google.common.collect.Multimap;
import gov.nysenate.sage.model.address.BuildingAddress;
import gov.nysenate.sage.model.address.Zip5;

import javax.annotation.Nonnull;
import java.util.List;

public interface PostOfficeDao {
    @Nonnull
    List<BuildingAddress> getPostOffices(int deliveryZip);

    /**
     * Clears the database table, and adds the given data.
     */
    void replaceData(Multimap<Zip5, BuildingAddress> postOfficeMap);
}

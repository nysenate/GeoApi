package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.model.address.PostOfficeAddress;

import java.util.List;

public interface PostOfficeDao {
    List<PostOfficeAddress> getAllPostOffices();

    /**
     * Clears the database table, and adds the given data.
     */
    void replaceData(List<PostOfficeAddress> newData);
}

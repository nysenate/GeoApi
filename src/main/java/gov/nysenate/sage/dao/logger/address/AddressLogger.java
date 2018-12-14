package gov.nysenate.sage.dao.logger.address;

import gov.nysenate.sage.model.address.Address;

public interface AddressLogger {

    /**
     * Inserts an address into the address table and returns the address id. If an exact match already exists
     * that address id is returned instead of inserting an identical entry.
     * @param address
     * @return int address id or -1 if not found
     */
    public int logAddress(Address address);

    /**
     * Attempts to retrieve the address id of the given address by
     * @param address
     * @return int address id or -1 if not found
     */
    public int getAddressId(Address address);
}

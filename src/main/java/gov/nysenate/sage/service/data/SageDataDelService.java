package gov.nysenate.sage.service.data;

public interface SageDataDelService {
    /**
     * Cleans up bad zips (Zips that are not 5 digits) from the geocache
     * Iterates from the offset to the end of the db
     * @return A success or error response
     */
    Object cleanUpBadZips(Integer offset);

    /**
     * Deletes states in the geocache that are
     * @return A success or error response
     */
    Object cleanUpBadStates();
}

package gov.nysenate.sage.adapter;

import gov.nysenate.sage.boe.AddressUtils;
import gov.nysenate.sage.boe.BOEAddressRange;
import gov.nysenate.sage.boe.BOEStreetAddress;
import gov.nysenate.sage.boe.DistrictLookup;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.service.DistrictService;
import gov.nysenate.sage.service.DistrictService.TYPE;
import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.service.DistrictService.DistAssignInterface;
import gov.nysenate.sage.service.DistrictService.DistException;
import gov.nysenate.sage.util.DB;
import gov.nysenate.sage.util.JsonUtil;
import org.apache.log4j.Logger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A Street file adapter implementation to resolve district codes.
 *
 * Street files are distributed by the Board of Elections on a county basis.
 * These files contain address ranges with corresponding district code information.
 * District information can be obtained quickly by matching a given Address to an
 * Address Range stored in the street file database.
 *
 * TODO: Replace Result with DistrictResult
 */

public class StreetData implements DistAssignInterface {

    public static final String source = StreetData.class.getSimpleName();

    /** Represents the status of the lookup result.
     *  Sorted by ascending order of accuracy. */
    public enum StreetDataStatus {
        NOMATCH,
        ZIP5,
        STREET,
        HOUSE
    }

    private final Logger logger = Logger.getLogger(this.getClass());
    private DistrictLookup streetFinder;

    public StreetData() throws Exception {
        this.streetFinder = new DistrictLookup(ApplicationFactory.getDataSource());
    }

    /**
     * Assign district information to a collection of Address objects.
     *
     * The process works by first parsing the raw string representation of the Address object
     * into address parts. The address parts are then used to obtain a street address range which
     * is searched against the street file database using the DistrictLookup methods.
     *
     * The resolution of the street matches will be one of the following:
     *
     *     HOUSE :  Address was found in a given house range on a street.
     *
     *     STREET:  Address was not in any house range but the street was found.
     *              The ranges on that street are consolidated such that the districts
     *              that are consistent across all ranges on that street will be returned.
     *
     *     ZIP:     Address was not found in a given house range nor the street.
     *              The ranges contained in the zipcode are consolidated and any consistent
     *              districts will be returned.
     *
     *     NOMATCH: Address was not found in the database. Better luck with GeoServer.
     *
     * The Result object will contain the supplied address with it's district information
     * populated, a list of unassigned district types in the messages list, as well as the status code:
     *
     *      'NOMATCH'
     *      'ZIP5'
     *      'STREET'
     *      'HOUSE'
     *
     * @see Result
     * @see DistrictLookup
     *
     * @param addresses     An ArrayList of Address objects.
     * @param types         A List of TYPE objects specifying which districts should be returned.
     * @return              An ArrayList of DistrictResult objects.
     */

    @Override
    public ArrayList<Result> assignDistricts(ArrayList<Address> addresses, List<DistrictService.TYPE> types) throws DistException {

        logger.info(String.format("Processing %d address(es) using street lookup.", addresses.size()));
        List<Result> streetFileResults = new ArrayList<>();

        for (Address address : addresses){

            Result result;
            String statusCode = StreetDataStatus.NOMATCH.name();
            BOEStreetAddress streetAddress = AddressUtils.parseAddress(address.as_raw());
            ArrayList<String> messages = new ArrayList<>();
            ArrayList<TYPE> unassignedTypes = new ArrayList<>(types);

            try {
                /** Attempt to match address by house */
                List<BOEAddressRange> matches = streetFinder.getRangesByHouse(streetAddress);
                if (matches.size() == 1) {
                    statusCode = StreetDataStatus.HOUSE.name();
                    unassignedTypes = streetFinder.setDistrictsForAddress(address, matches.get(0), types);
                }

                /** Then try a street file lookup by street and consolidate */
                else if (streetAddress.street != null && !streetAddress.street.trim().isEmpty()) {
                    matches = streetFinder.getRangesByStreet(streetAddress);
                    BOEAddressRange consolidated = streetFinder.consolidateRanges(matches);
                    if (consolidated != null) {
                        statusCode = StreetDataStatus.STREET.name();
                        unassignedTypes = streetFinder.setDistrictsForAddress(address, consolidated, types);
                    }
                }

                /** Finally try a street file lookup by zip5 and consolidate */
                else if (streetAddress.zip5 != 0){
                    matches = streetFinder.getRangesByZip(streetAddress);
                    BOEAddressRange consolidated = streetFinder.consolidateRanges(matches);
                    if (consolidated != null) {
                        statusCode = StreetDataStatus.STREET.name();
                        unassignedTypes = streetFinder.setDistrictsForAddress(address, consolidated, types);
                    }
                }

                /** Append the unassigned district type names to the messages list
                 *  TODO: Result object should be modified to hold this data more clearly. */
                for(TYPE unassignedType : unassignedTypes){
                    messages.add(unassignedType.name());
                }

                /** Create the result */
                result = new Result(address, messages);
                result.status_code = statusCode;
                result.source = source;

                /** Append the results to the Result collection */
                streetFileResults.add(result);
            }
            catch (SQLException sqlException){
                logger.error("An SQL Exception occurred during street data lookup with error " + sqlException.getMessage());
                throw new DistException(sqlException.getMessage(), sqlException);
            }
        }

        return (ArrayList) streetFileResults;
    }

    /**
     * @param address        An Address object.
     * @param type           A TYPE object specifying the district to return.
     * @return               Result
     * @throws DistException
     */
    @Override
    public Result assignDistrict(Address address, TYPE type) throws DistException {
         return assignDistrict(new ArrayList<Address>(Arrays.asList(new Address[]{address})), type).get(0);
    }

    /**
     * @param addresses      An ArrayList of Address objects.
     * @param type           A TYPE object specifying the district to return.
     * @return               ArrayList Result
     * @throws DistException
     */
    @Override
    public ArrayList<Result> assignDistrict(ArrayList<Address> addresses, TYPE type) throws DistException {
        return assignDistricts(addresses, new ArrayList<TYPE>(Arrays.asList(type)));
    }

    /**
     * @param address        An Address object.
     * @param types          A List of TYPE objects specifying which districts should be returned.
     * @return Result        Result
     * @throws DistException
     */
    @Override
    public Result assignDistricts(Address address, List<DistrictService.TYPE> types) throws DistException {
        return assignDistricts(new ArrayList<Address>(Arrays.asList(new Address[]{address})),types).get(0);
    }
}
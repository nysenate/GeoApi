package gov.nysenate.sage.dao.provider.streetfile;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.address.Zip5;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

public interface StreetfileDao {
    String nullString();

    List<DistrictType> order();

    void replaceStreetfile(Path streetfilePath) throws SQLException, IOException;

    /**
     * Performs a lookup in the streetfile table, consolidating districts if needed.
     * @return a districted address, with the highest possible match level.
     */
    DistrictInfo getDistrictInfo(Address addr);

    /**
     * Returns a list of street ranges with district information for a given zip5.
     * @return List of DistrictedStreetRange
     */
    List<DistrictedStreetRange> getDistrictStreetRangesByZip(Zip5 zip5);
}

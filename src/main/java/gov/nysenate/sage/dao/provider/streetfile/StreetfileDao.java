package gov.nysenate.sage.dao.provider.streetfile;

import gov.nysenate.sage.model.address.BuildingAddress;
import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;

import javax.annotation.Nonnull;
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
     * @param matchLevel the highest DistrictMatchLevel to attempt.
     * @return a districted address, with the highest possible match level.
     */
    DistrictInfo getDistrictInfo(BuildingAddress addr, @Nonnull DistrictMatchLevel matchLevel);

    /**
     * Returns a list of street ranges with district information for a given zip5.
     * @return List of DistrictedStreetRange
     */
    List<DistrictedStreetRange> getDistrictStreetRangesByZip(Integer zip5);
}

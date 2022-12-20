package gov.nysenate.sage.service.data;

import gov.nysenate.sage.scripts.streetfinder.NamePair;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.List;

public interface SageDataGenService {

    /**
     * Used to help fill in streetfiles.
     * @param townCodes true if we're getting the town codes, false if it's the county codes.
     * @return the relevant pairs of data.
     */
    List<NamePair> getCodes(boolean townCodes);

    /**
     * Manually Updates the senator cache used by the front end
     */
    void updateSenatorCache();

    /**
     * Gnerates the District meta data for the Senate, Assembly, and Congressional Candidates
     * @param option
     * @return
     * @throws IOException
     * @throws XmlRpcException
     */
    Object generateMetaData(String option) throws IOException, XmlRpcException;
}

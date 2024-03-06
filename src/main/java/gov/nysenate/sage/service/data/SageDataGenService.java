package gov.nysenate.sage.service.data;

import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.Map;

public interface SageDataGenService {

    /**
     * Used to help fill in streetfiles.
     * @param townCodes true if we're getting the town codes, false if it's the county codes.
     * @return the relevant mapping of data.
     */
    Map<String, String> getCodes(boolean townCodes);

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

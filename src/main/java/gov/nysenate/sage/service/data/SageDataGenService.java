package gov.nysenate.sage.service.data;

import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;

public interface SageDataGenService {
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

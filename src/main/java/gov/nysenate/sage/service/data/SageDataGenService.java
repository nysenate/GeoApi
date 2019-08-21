package gov.nysenate.sage.service.data;

import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;

public interface SageDataGenService {

    /**
     * Ensures that a town code file is in the proper directory for use in street file generation
     * @return boolean
     */
    public boolean ensureTownCodeFile();

    /**
     * Ensures that a county code file is in the proper directory for use in street file generation
     * @return boolean
     */
    public boolean ensureCountyCodeFile();

    /**
     * Generates Senator Images at the specified path and height
     * @param path
     * @param height
     * @return
     */
    public Object generateSenatorImages(String path, int height);

    /**
     * Gnerates the District meta data for the Senate, Assembly, and Congressional Candidates
     * @param option
     * @return
     * @throws IOException
     * @throws XmlRpcException
     */
    public Object generateMetaData(String option) throws IOException, XmlRpcException;
}

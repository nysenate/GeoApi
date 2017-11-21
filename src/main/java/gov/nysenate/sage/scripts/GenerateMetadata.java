package gov.nysenate.sage.scripts;

import gov.nysenate.sage.dao.model.AssemblyDao;
import gov.nysenate.sage.dao.model.CongressionalDao;
import gov.nysenate.sage.dao.model.SenateDao;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.district.Assembly;
import gov.nysenate.sage.model.district.Congressional;
import gov.nysenate.sage.util.AssemblyScraper;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.CongressScraper;
import gov.nysenate.services.NYSenateClientService;
import gov.nysenate.services.NYSenateJSONClient;
import gov.nysenate.services.model.Senator;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.List;

/**
 * SAGE contains several metadata tables which include information about different
 * district members such as senators. Running this script will retrieve the most
 * up to date information and refresh the contents of the local database. This script
 * must be run when setting up a new SAGE instance or when data has been changed.
 *
 * @author Ken Zalewski, Ash Islam
 */
public class GenerateMetadata
{
    private Config config;

    public GenerateMetadata()
    {
        config = ApplicationFactory.getConfig();
    }

    public static void main(String[] args) throws Exception
    {
        boolean updated = false;

//        if (args.length == 0) {
//            System.err.println("Usage: GenerateMetadata [--all] [--assembly|-a] [--congress|-c] [--senate|-s]");
//            System.exit(1);
//        }

        /** Load up the configuration settings */
        if (!ApplicationFactory.bootstrap()){
            System.err.println("Failed to configure application");
            System.exit(-1);
        }

        GenerateMetadata generateMetadata = new GenerateMetadata();

        boolean processAssembly = true;
        boolean processCongress = true;
        boolean processSenate = true;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--all")) {
                processAssembly = true;
                processSenate = true;
                processCongress = true;
            }
            else if (arg.equals("--assembly") || arg.equals("-a")) {
                processAssembly = true;
            }
            else if (arg.equals("--congress") || arg.equals("-c")) {
                processCongress = true;
            }
            else if (arg.equals("--senate") || arg.equals("-s")) {
                processSenate = true;
            }
            else {
                System.err.println(arg+": Invalid option");
                System.exit(1);
            }
        }

        if (processAssembly) {
            updated = generateMetadata.generateAssemblyData();
        }

        if (processCongress) {
            updated = generateMetadata.generateCongressionalData();
        }

        if (processSenate) {
            updated = generateMetadata.generateSenateData();
        }

        ApplicationFactory.close();
        if (updated) System.exit(0);
        else System.exit(1);
    }

     /**
     * Retrieves Congressional member data from an external source and updates the
     * relevant data in the database.
     */
    private boolean generateCongressionalData()
    {
        boolean updated = false;

        System.out.println("Indexing NY Congress by scraping its website...");
        CongressionalDao congressionalDao = new CongressionalDao();

        /** Retrieve the congressional members and insert into the database */
        List<Congressional> congressionals = CongressScraper.getCongressionals();
        for (Congressional congressional : congressionals) {
            int district = congressional.getDistrict();
            Congressional existingCongressional = congressionalDao.getCongressionalByDistrict(district);

            if (existingCongressional == null) {
                updated = true;
                congressionalDao.insertCongressional(congressional);
            }
            else if (isCongressionalDataUpdated(existingCongressional, congressional)) {
                updated = true;
                congressionalDao.deleteCongressional(district);
                congressionalDao.insertCongressional(congressional);
            }
        }
        return updated;
    }

    /**
     * Retrieves Assembly member data from an external source and updates the
     * relevant data in the database.
     */
    private boolean generateAssemblyData()
    {
        System.out.println("Indexing NY Assembly by scraping its website...");
        boolean updated = false;
        AssemblyDao assemblyDao = new AssemblyDao();

        /** Retrieve the assemblies and insert into the database */
        List<Assembly> assemblies = AssemblyScraper.getAssemblies();
        for (Assembly assembly : assemblies) {
            int district = assembly.getDistrict();
            Assembly existingAssembly = assemblyDao.getAssemblyByDistrict(district);

            if (existingAssembly == null) {
                updated = true;
                assemblyDao.insertAssembly(assembly);
            }
            else if (isAssemblyDataUpdated(existingAssembly, assembly)) {
                updated = true;
                assemblyDao.deleteAssemblies(district);
                assemblyDao.insertAssembly(assembly);
            }
        }
        return updated;
    }

    /**
     * Retrieves senate data from the NY Senate API Client and stores it in
     * the database.
     * @throws XmlRpcException
     */
    private boolean generateSenateData() throws XmlRpcException, IOException {
        boolean updated = false;

        NYSenateClientService senateClient;
        SenateDao senateDao = new SenateDao();

        System.out.println("Generating senate data from NY Senate client services");

        /** Obtain the senate client service */
        String domain = config.getValue("nysenate.domain", "http://www.nysenate.gov");
        senateClient = new NYSenateJSONClient(domain);

        /** Retrieve the list of senators from the client API */
        List<Senator> senators = senateClient.getSenators();

        for (Senator senator : senators) {
            int district = senator.getDistrict().getNumber();
            if (district > 0) {
                Senator existingSenator = senateDao.getSenatorByDistrict(district);
                if (existingSenator == null) {
                    senateDao.insertSenate(senator.getDistrict());
                    senateDao.insertSenator(senator);
                }
                else {
                    senateDao.deleteSenator(district);
                    senateDao.insertSenator(senator);
                }
                updated = true;
            }
        }
        return updated;
    }

    private boolean isCongressionalDataUpdated(Congressional c1, Congressional c2) {
        if (c1 != null && c2 != null) {
            if (!(c1.getDistrict() == c2.getDistrict() &&
                    c1.getMemberName().equals(c2.getMemberName()) &&
                    c1.getMemberUrl().equals(c2.getMemberUrl())))
            {
                System.out.println("Congressional " + c1.getDistrict() + "updated.");
                return true;
            }
        }
        else if (c1 == null && c2 != null) {
            return true;
        }
        return false;
    }

    private boolean isAssemblyDataUpdated(Assembly a1, Assembly a2) {
        if (a1 != null && a2 != null) {
            if (!(a1.getDistrict() == a2.getDistrict() &&
                a1.getMemberName().equals(a2.getMemberName()) &&
                a1.getMemberUrl().equals(a2.getMemberUrl())))
            {
                System.out.println("Assembly " + a1.getDistrict() + " updated.");
                return true;
            }
        }
        else if (a1 == null && a2 != null) {
            return true;
        }
        return false;
    }
}


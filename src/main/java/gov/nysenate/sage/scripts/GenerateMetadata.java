package gov.nysenate.sage.scripts;

import gov.nysenate.sage.dao.model.AssemblyDao;
import gov.nysenate.sage.dao.model.CongressionalDao;
import gov.nysenate.sage.dao.model.SenateDao;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.district.Assembly;
import gov.nysenate.sage.model.district.Congressional;
import gov.nysenate.sage.util.*;
import gov.nysenate.services.MemoryCachedNYSenateClient;
import gov.nysenate.services.model.Senator;
import org.apache.xmlrpc.XmlRpcException;

import java.util.List;

/**
 * SAGE contains several metadata tables which include information about different
 * district members such as senators. Running this script will retrieve the most
 * up to date information and refresh the contents of the local database. This script
 * must be run when setting up a new SAGE instance.
 *
 * @author Ken Zalewski, Ash Islam
 */
public class GenerateMetadata
{
    private Config config;

    public GenerateMetadata()
    {
        config = ApplicationFactory.getConfig();
    } // GenerateMetadata()


    public static void main(String[] args) throws Exception
    {
        if (args.length == 0) {
            System.err.println("Usage: GenerateMetadata [--all] [--assembly|-a] [--congress|-c] [--senate|-s]");
            System.exit(1);
        }

        /** Load up the configuration settings */
        if (!ApplicationFactory.bootstrap()){
            System.err.println("Failed to configure application");
            System.exit(-1);
        }

        GenerateMetadata generateMetadata = new GenerateMetadata();

        boolean processAssembly = false;
        boolean processCongress = false;
        boolean processSenate = false;

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
            generateMetadata.generateAssemblyData();
        }

        if (processCongress) {
            generateMetadata.generateCongressionalData();
        }

        if (processSenate) {
            generateMetadata.generateSenateData();
        }
    } // main()


    /**
     * Retrieves Congressional member data from an external source and updates the
     * relevant data in the database.
     */
    private void generateCongressionalData()
    {
        System.out.println("Indexing NY Congress by scraping its website...");
        CongressionalDao congressionalDao = new CongressionalDao();

        /** Purge the existing congressional members */
        congressionalDao.deleteCongressionals();

        /** Retrieve the congressional members and insert into the database */
        List<Congressional> congressionals = CongressScraper.getCongressionals();
        for (Congressional congressional : congressionals) {
            congressionalDao.insertCongressional(congressional);
        }
    } // generateCongressionalData()


    /**
     * Retrieves Assembly member data from an external source and updates the
     * relevant data in the database.
     */
    private void generateAssemblyData()
    {
        System.out.println("Indexing NY Assembly by scraping its website...");
        AssemblyDao assemblyDao = new AssemblyDao();

        /** Purge the existing assembly members */
        assemblyDao.deleteAssemblies();

        /** Retrieve the assemblies and insert into the database */
        List<Assembly> assemblies = AssemblyScraper.getAssemblies();
        for (Assembly assembly : assemblies) {
            assemblyDao.insertAssembly(assembly);
        }
    } // generateAssemblyData()


    /**
     * Retrieves senate data from the NY Senate API Client and stores it in
     * the database.
     * @throws XmlRpcException
     */
    private void generateSenateData() throws XmlRpcException
    {
        MemoryCachedNYSenateClient senateClient;
        SenateDao senateDao = new SenateDao();

        String key = config.getValue("nysenate.key");
        String domain = config.getValue("nysenate.domain");

        System.out.println("Generating senate data from NY Senate client services");

        /** Obtain the senate client service */
        senateClient = new MemoryCachedNYSenateClient(domain, key);

        /** Purge the existing tables */
        senateDao.deleteSenators();
        senateDao.deleteSenateDistricts();

        /** Retrieve the list of senators from the client API */
        List<Senator> senators = senateClient.getSenators();

        for (Senator senator : senators) {
            /** Senate table contains the district and the url */
            senateDao.insertSenate(senator.getDistrict());

            /** Senator table will contain all of the senator information */
            senateDao.insertSenator(senator);
        }
    } // generateSenateData()
}


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
import gov.nysenate.services.MemoryCachedNYSenateClient;
import gov.nysenate.services.model.District;
import gov.nysenate.services.model.Office;
import gov.nysenate.services.model.Senator;
import org.apache.xmlrpc.XmlRpcException;

import java.util.ArrayList;
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
    private boolean generateSenateData() throws XmlRpcException
    {
        boolean updated = false;

        MemoryCachedNYSenateClient senateClient;
        SenateDao senateDao = new SenateDao();

        String key = config.getValue("nysenate.key");
        String domain = config.getValue("nysenate.domain");

        System.out.println("Generating senate data from NY Senate client services");

        /** Obtain the senate client service */
        senateClient = new MemoryCachedNYSenateClient(domain, key);

        /** Retrieve the list of senators from the client API */
        List<Senator> senators = senateClient.getSenators();

        for (Senator senator : senators) {
            int district = senator.getDistrict().getNumber();
            Senator existingSenator = senateDao.getSenatorByDistrict(district);

            if (existingSenator == null) {
                updated = true;
                senateDao.insertSenate(senator.getDistrict());
                senateDao.insertSenator(senator);
            }
            else if (isSenatorDataUpdated(existingSenator, senator)) {
                updated = true;
                senateDao.deleteSenator(district);
                senateDao.insertSenator(senator);
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

    private boolean isSenatorDataUpdated(Senator s1, Senator s2) {
        if (s1 != null && s2 != null) {
            if (!(s1.getShortName().equals(s2.getShortName()) &&
                s1.getImageUrl().equals(s2.getImageUrl()) &&
                s1.getAdditionalContact().equals(s2.getAdditionalContact()) &&
                s1.getEmail().equals(s2.getEmail()) &&
                s1.getLastName().equals(s2.getLastName()) &&
                s1.getName().equals(s2.getName()) &&
                s1.getUrl().equals(s2.getUrl())))
            {
                System.out.println("Basic senator information has changed for " + s1.getName());
                return true;
            }

            District d1 = s1.getDistrict();
            District d2 = s2.getDistrict();
            if (!(d1.getNumber() == d2.getNumber() &&
                d1.getUrl().equals(d2.getUrl()) &&
                d1.getImageUrl().equals(d2.getImageUrl()) &&
                d1.getMapUrl().equals(d2.getMapUrl())))
            {
                System.out.println("District information has changed for " + s1.getName());
                return true;
            }

            ArrayList<Office> offices1 = s1.getOffices();
            ArrayList<Office> offices2 = s2.getOffices();
            if (offices1.size() == offices2.size()) {
                for (int i = 0; i < offices2.size(); i++) {
                    Office office1 = offices1.get(i);
                    Office office2 = offices2.get(i);

                    if (!(office1.getName().equals(office2.getName()) &&
                        office1.getAdditional().equals(office2.getAdditional()) &&
                        office1.getStreet().equals(office2.getStreet()) &&
                        office1.getCity().equals(office2.getCity()) &&
                        office1.getPostalCode().equals(office2.getPostalCode()) &&
                        office1.getCountry().equals(office2.getCountry()) &&
                        office1.getCountryName().equals(office2.getCountryName()) &&
                        office1.getLatitude() == office2.getLatitude() &&
                        office1.getLongitude() == office2.getLongitude() &&
                        office1.getPhone().equals(office2.getPhone()) &&
                        office1.getOtherPhone().equals(office2.getOtherPhone()) &&
                        office1.getFax().equals(office2.getFax()) &&
                        office1.getProvince().equals(office2.getProvince()) &&
                        office1.getProvinceName().equals(office2.getProvinceName())))
                    {
                        System.out.println("Office information has changed for " + s1.getName() + ": " + office1.getName());
                        return true;
                    }
                }
            }
            else {
                System.out.println("Offices did not match for " + s1.getName());
                return true;
            }
        }
        else if (s1 == null && s2 != null) {
            return true;
        }
        return false;
    }
}


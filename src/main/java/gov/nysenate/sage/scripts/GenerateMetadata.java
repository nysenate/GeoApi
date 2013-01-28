package gov.nysenate.sage.scripts;

import gov.nysenate.sage.util.AssemblyScraper;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.CongressScraper;
import gov.nysenate.sage.util.Connect;
import gov.nysenate.sage.util.SenateDistrictMap;
import gov.nysenate.sage.util.NYSenateServices;

import java.io.File;


public class GenerateMetadata
{
  private static final int MAX_DISTRICTS = 2;
  //private static final int MAX_DISTRICTS = 63;

  public static void main(String[] args) throws Exception
  {
    if (args.length == 0) {
      System.err.println("Usage: GenerateMetadata [--all] [--assembly|-a] [--congress|-c] [--senate|-s] [--json|-j]");
      System.exit(1);
    }

    boolean processAssembly = false;
    boolean processCongress = false;
    boolean processSenate = false;
    boolean generateJson = false;

    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if (arg.equals("--all")) {
        processAssembly = true;
        processSenate = true;
        processCongress = true;
        generateJson = true;
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
      else if (arg.equals("--json") || arg.equals("-j")) {
        generateJson = true;
      }
      else {
        System.err.println(arg+": Invalid option");
        System.exit(1);
      }
    }

    Connect dbconn = new Connect();

    if (processAssembly) {
      System.out.println("Indexing NY Assembly by scraping its website...");
      new AssemblyScraper().index(dbconn);
    }
    if (processCongress) {
      System.out.println("Indexing US Congress by scraping its website...");
      new CongressScraper().index(dbconn);
    }
    if (processSenate) {
      System.out.println("Indexing NY Senate by connecting to NYSenate.gov...");
      new NYSenateServices().index(dbconn);
    }

    dbconn.close();
    dbconn = null;

    if (generateJson) {
      File writeDir = new File(Config.read("district_maps.dir"));
      System.out.println("Generating map data for all NYSenate districts; output will be in "+writeDir.getCanonicalPath());
      generateDistrictMapData(writeDir);
    }
  }


  private static void generateDistrictMapData(File outDir)
  {
    outDir.mkdirs();
    for (int distnum = 1; distnum <= MAX_DISTRICTS; distnum++) {
      SenateDistrictMap districtMap = new SenateDistrictMap(distnum);
      String fileName = String.format("sd%02d", distnum);
      File outFile = new File(outDir, fileName);
      districtMap.loadCoordinates();
      districtMap.writeCoordinates(outFile);
    }
  } // generateDistrictMapData()
}


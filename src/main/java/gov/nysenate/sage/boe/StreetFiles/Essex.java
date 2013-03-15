package gov.nysenate.sage.boe.StreetFiles;

import gov.nysenate.sage.boe.BOEAddressRange;
import gov.nysenate.sage.boe.StreetFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;


public class Essex extends StreetFile
{
  public int currentLine;
  public HashMap<String, String> townMap;

  public Essex(int county, File street_file) throws Exception
  {
    super(county, street_file);

    townMap = new HashMap<String, String>();
    townMap.put("CHESTER", "CHESTF");
    townMap.put("CROWN P", "CROWN");
    townMap.put("ELIZABE", "ELIZAB");
    townMap.put("ESSEX", "ESSEX");
    townMap.put("JAY", "JAY");
    townMap.put("KEENE", "KEENE");
    townMap.put("LEWIS", "LEWIS");
    townMap.put("MINERVA", "MINERV");
    townMap.put("MORIAH", "MORIAH");
    townMap.put("NEWCOMB", "NEWCOM");
    townMap.put("NORTH E", "N ELBA");
    townMap.put("NORTH H", "N HUDS");
    townMap.put("SCHROON", "SCHROO");
    townMap.put("ST. ARM", "ST ARM");
    townMap.put("TICONDE", "TICOND");
    townMap.put("WESTPOR", "WESTPO");
    townMap.put("WILLSBO", "WILLSB");
    townMap.put("WILMING","WILMIN");
  }


  @Override
  public void save(DataSource db) throws Exception
  {
    logger.info("Starting Essex");
    currentLine = 0;
    QueryRunner runner = new QueryRunner(db);
    BufferedReader br = new BufferedReader(new FileReader(street_file));

    String line;
    String[] parts;
    br.readLine(); // Skip the header
    runner.update("BEGIN");
    while ((line = br.readLine()) != null) {
      parts = line.split("\t");
      BOEAddressRange range = new BOEAddressRange();
      range.street = parts[0];
      if (!parts[1].isEmpty()) {
        range.zip5 = Integer.parseInt(parts[1]);
      }
      range.bldgLoNum = Integer.parseInt(parts[2]);
      range.bldgHiNum = Integer.parseInt(parts[3]);
      range.bldgParity = getParity(parts[4]);
      range.town = parts[5];
      range.townCode = getTownCode(range.town);
      range.electionCode = Integer.parseInt(parts[6]);
      range.assemblyCode = Integer.parseInt(parts[7]);
      range.congressionalCode = Integer.parseInt(parts[8]);
      range.senateCode = Integer.parseInt(parts[9]);
      range.countyCode = this.county_code;
      range.state = "NY";
      save_record(range, db);

      if (++currentLine % 5000 == 0) {
        runner.update("COMMIT");
        runner.update("BEGIN");
      }
    }
    runner.update("COMMIT");
    br.close();
    logger.info("Done with Essex");
  }


  public String getTownCode(String town)
  {
    String abbrev = town.substring(0, Math.min(town.length(),7));
    if (townMap.containsKey(abbrev)) {
      return townMap.get(abbrev);
    }
    else {
      throw new RuntimeException("Line "+currentLine+": Town "+town+" not found in the town code map.");
    }
  }


  public String getParity(String parity)
  {
    if (parity.equals("Even and Odd Numbers")) {
      return "ALL";
    }
    else if (parity.equals("Even Numbers")) {
      return "EVENS";
    }
    else if (parity.equals("Odd Numbers")) {
      return "ODDS";
    }
    else {
      logger.error("Line "+currentLine+": Invalid parity input "+parity);
      return "ALL";
    }
  }
}

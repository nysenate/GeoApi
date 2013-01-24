package gov.nysenate.sage.boe.StreetFiles;

import gov.nysenate.sage.boe.BOEAddressRange;
import gov.nysenate.sage.boe.StreetFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;

public class Erie extends StreetFile {

    HashMap<String, String> townMap;
    HashMap<String, String> townCodeMap;

    public Erie(int county_code, File street_file) throws Exception {
        super(county_code, street_file);

        townMap = new HashMap<String, String>();
        townMap.put("ALDN","Alden");
        townMap.put("AMHS","Amherst");
        townMap.put("AURA","Aurora");
        townMap.put("BFLO","Buffalo");
        townMap.put("BOST","Boston");
        townMap.put("BRNT","Brant");
        townMap.put("CKTW","Cheektowaga");
        townMap.put("CLAR","Clarence");
        townMap.put("CLDN","Colden");
        townMap.put("COLL","Collins");
        townMap.put("CONC","Concord");
        townMap.put("CTON","City of Tonowanda");
        townMap.put("EDEN","Eden");
        townMap.put("ELMA","Elma");
        townMap.put("EVNS","Evans");
        townMap.put("GRIS","Grand Island");
        townMap.put("HAMB","Hamburg");
        townMap.put("HOLL","Holland");
        townMap.put("LACK","Lackawanna");
        townMap.put("LANC","Lancaster");
        townMap.put("MARL","Marilla");
        townMap.put("NCOL","North Collins");
        townMap.put("NEWS","Newstead");
        townMap.put("ORPK","Orchard Park");
        townMap.put("SARD","Sardinia");
        townMap.put("TTON","Town of Tonowanda");
        townMap.put("WALS","Wales");
        townMap.put("WSEN","West Seneca");

        townCodeMap = new HashMap<String, String>();
        townCodeMap.put("ALDN","ALDEN");
        townCodeMap.put("AMHS","AMHERS");
        townCodeMap.put("AURA","AURORA");
        townCodeMap.put("BFLO","-BUFFA");
        townCodeMap.put("BOST","BOSTON");
        townCodeMap.put("BRNT","BRANT");
        townCodeMap.put("CKTW","CHEEKT");
        townCodeMap.put("CLAR","CLAREN");
        townCodeMap.put("CLDN","COLDEN");
        townCodeMap.put("COLL","COLLIN");
        townCodeMap.put("CONC","CONCOR");
        townCodeMap.put("CTON","-TONAW");
        townCodeMap.put("EDEN","EDEN");
        townCodeMap.put("ELMA","ELMA");
        townCodeMap.put("EVNS","EVNS");
        townCodeMap.put("GRIS","GRIS");
        townCodeMap.put("HAMB","HAMBUR");
        townCodeMap.put("HOLL","HOLLAN");
        townCodeMap.put("LACK","-LACKA");
        townCodeMap.put("LANC","LANCAS");
        townCodeMap.put("MARL","MARILL");
        townCodeMap.put("NCOL","NCOL");
        townCodeMap.put("NEWS","NEWSTE");
        townCodeMap.put("ORPK","ORPK");
        townCodeMap.put("SARD","SARDIN");
        townCodeMap.put("TTON","TONAWA");
        townCodeMap.put("WALS","WALES");
        townCodeMap.put("WSEN","W SENE");
    }

    public String getParity(String parityCode) {
        if (parityCode.equals("B") || parityCode.equals("N")) {
            return "ALL";
        } else if (parityCode.equals("O")) {
            return "ODDS";
        } else if (parityCode.equals("E")) {
            return "EVENS";
        } else {
            logger.warn("Parity code "+parityCode+" not one of [BNOE]");
            return "ALL";
        }
    }

    public String getTown(String townId) {
        if (townMap.containsKey(townId)) {
            return townMap.get(townId);
        } else {
            logger.warn("Unrecognized town code "+townId+" in townMap");
            return "";
        }
    }

    public String getTownCode(String townId) {
        if (townCodeMap.containsKey(townId)) {
            return townCodeMap.get(townId);
        } else {
            logger.warn("Unrecognized town code "+townId+" in townCodeMap");
            return "";
        }
    }


    public int getDistrict(String district) {
        return Integer.parseInt(district.substring(3));
    }

    @Override
    public void save(DataSource db) throws Exception {
        logger.info("Starting "+street_file.getName());
        BufferedReader br = new BufferedReader(new FileReader(street_file));
        QueryRunner runner = new QueryRunner(db);

        String line;
        runner.update("BEGIN");
        br.readLine(); // skip header
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            BOEAddressRange range = new BOEAddressRange();
            range.zip5 = Integer.parseInt(parts[4]);
            range.street= (parts[0].trim()+" "+parts[1].trim()+" "+parts[2].trim()+" "+parts[3].trim()).trim();
            range.bldgLoNum = Integer.parseInt(parts[5]);
            range.bldgHiNum = Integer.parseInt(parts[6]);
            range.bldgParity = getParity(parts[7]);
            range.townCode = getTownCode(parts[8]);
            range.town = getTown(parts[8]);
            range.electionCode = Integer.parseInt(parts[10]);
            range.congressionalCode = getDistrict(parts[11]);
            range.senateCode = getDistrict(parts[12]);
            range.assemblyCode = getDistrict(parts[13]);
            range.clegCode = getDistrict(parts[14]);
            range.countyCode = county_code;
            range.state = "NY";
            save_record(range, db);
        }
        br.close();
        runner.update("COMMIT");

        logger.info("Done with "+street_file.getName());
    }

}

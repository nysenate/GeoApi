package gov.nysenate.sage.boe.StreetFiles;

import gov.nysenate.sage.boe.BOEAddressRange;
import gov.nysenate.sage.boe.StreetFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;

public class Wyoming extends StreetFile {
    private final File street_index;
    private final HashMap<String, String> townMap;

    public Wyoming(int countyCode, File street_index) throws Exception {
        super(countyCode, street_index);
        this.street_index = street_index;

        townMap = new HashMap<String, String>();
        townMap.put("1", "ARCADE");
        townMap.put("2", "ATTICA");
        townMap.put("3", "BENNIN");
        townMap.put("4", "CASTIL");
        townMap.put("5", "COVING");
        townMap.put("6", "EAGLE");
        townMap.put("7", "GAINEV");
        townMap.put("8", "GENESF");
        townMap.put("9", "JAVA");
        townMap.put("10", "MIDDLE");
        townMap.put("11", "ORANGV");
        townMap.put("12", "PERRY");
        townMap.put("13", "PIKE");
        townMap.put("14", "SHELDO");
        townMap.put("15", "WARSAW");
        townMap.put("16", "WETHER");
    }

    public boolean isStartPage(String line) {
        return line.contains("===============");
    }

    public boolean isEndPage(String line) {
        return line.contains("") || line.contains("========");
    }

    public String getTownCode(String townId) {
        if (townMap.containsKey(townId)) {
            return townMap.get(townId);
        } else {
            logger.warn("TownId "+townId+" not found in townMap");
            return "";
        }
    }

    public String getParity(String parityId) {
        if (parityId.equals("B")) {
            return "ALL";
        } else if (parityId.equals("O")) {
            return "ODDS";
        } else if (parityId.equals("E")) {
            return "EVENS";
        } else {
            logger.warn("parityId '"+parityId+"' not one of B/O/E");
            return "";
        }
    }

    @Override
    public void save(DataSource db) throws Exception {
        logger.info("Starting "+street_file.getName());
        Pattern linePattern = Pattern.compile("^([0-9]{1,2})([0-9]{2})([0-9]{2})-[0-9]\\s+([NESW])?\\s+((?:[A-Z0-9'&#]+ )+)\\s+([A-Z]+)?\\s+([NESW])?\\s+((?:[A-Z]+ )+)\\s+([0-9]+)\\s+([0-9]+)\\s+([BOE])\\s+([0-9]{5})$");
        QueryRunner runner = new QueryRunner(db);
        BufferedReader br = new BufferedReader(new FileReader(street_index));
        String line;

        int counter = 0;
        runner.update("BEGIN");
        while ((line = br.readLine()) != null) {
            if (isStartPage(line)) {
                while ((line=br.readLine())!=null) {
                    if (line.isEmpty())
                        continue;
                    else if (isEndPage(line))
                        break;

                    Matcher lineMatcher = linePattern.matcher(line);
                    if (lineMatcher.find()) {
                        BOEAddressRange range = new BOEAddressRange();
                        String predir = lineMatcher.group(4)!=null ? lineMatcher.group(4) : "";
                        String street = lineMatcher.group(5)!=null ? lineMatcher.group(5) : "";
                        String suffix = lineMatcher.group(6)!=null ? lineMatcher.group(6) : "";
                        String postdir = lineMatcher.group(7)!=null ? lineMatcher.group(7) : "";
                        range.street = (predir+" "+street+" "+suffix+" "+postdir).trim();
                        range.town = lineMatcher.group(8);
                        range.zip5 = Integer.parseInt(lineMatcher.group(12));
                        range.state = "NY";
                        range.bldgLoNum = Integer.parseInt(lineMatcher.group(9));
                        range.bldgHiNum = Integer.parseInt(lineMatcher.group(10));
                        range.bldgParity = getParity(lineMatcher.group(11));
                        range.townCode = getTownCode(lineMatcher.group(1));
                        range.wardCode = Integer.parseInt(lineMatcher.group(2));
                        range.electionCode = Integer.parseInt(lineMatcher.group(3));
                        range.assemblyCode = 147;
                        range.senateCode = 59;
                        range.congressionalCode = 27;
                        range.countyCode = 56;
                        save_record(range, db);

                        if (++counter % 5000 == 0) {
                            runner.update("COMMIT");
                            runner.update("BEGIN");
                        }
                    } else {
                        logger.warn("linePattern match failure on: "+line);
                    }
                }
            }
        }
        runner.update("COMMIT");
        br.close();
        logger.info("Done with "+street_file.getName());
    }
}
/*

     // map array Input to database structure
     foreach ($Keys as $key => $value) {
         $count = count($value);
         // var_dump($count);

         // messy error checking,
         // 11 = there isn't a street type (RD, ST etc), 135 of these
         // 10 =  sometimes there isn't a Street type or road, only 1 of them
         if($count == 12 ){
             $Output[$key] = array(
                 'street' =>  $value[5].' '.$value[6].' '.$value[7], // Street Name
                 'zip5' =>  $value[11], // Zip Code
                 'bldg_lo_num' =>  $value[9], // House Range Begins
                 'bldg_hi_num' =>  preg_replace('/O|E|B/','', $value[10]), // House Range Ends
                 'bldg_parity' =>  partiy($value[10]), // Includes
                 'town_code' => townMap($value[1]), // Town Code
                 'town' =>  $value[8], // Town
                 'election_code' =>  $value[3], // Electoral Dist
                 'assembly_code' =>  147, // Assembly Dist = 147 from wyoming county street file 2
                 'congressional_code' =>  27, // Congressional Dist = 27 from wyoming county street file 2
                 'senate_code' =>  59, // Senate Dist = 59 from wyoming county street file 2
                 'county_code' =>  56, // County Code
                 'state' =>  'NY' // State
             );

         }elseif($count == 11 ){
             $Output[$key] = array(
                 'street' =>  $value[5].' '.$value[6], // Street Name
                 'zip5' =>  $value[10], // Zip Code
                 'bldg_lo_num' =>  $value[8], // House Range Begins
                 'bldg_hi_num' =>  preg_replace('/O|E|B/','', $value[9]), // House Range Ends
                 'bldg_parity' =>  partiy($value[9]), // Includes
                 'town_code' => townMap($value[1]), // Town Code
                 'town' =>  $value[7], // Town
                 'election_code' =>  $value[3], // Electoral Dist
                 'assembly_code' =>  147, // Assembly Dist = 147 from wyoming county street file 2
                 'congressional_code' =>  27, // Congressional Dist = 27 from wyoming county street file 2
                 'senate_code' =>  59, // Senate Dist = 59 from wyoming county street file 2
                 'county_code' =>  56, // County Code
                 'state' =>  'NY' // State

             );
         }elseif($count == 10 ){
             $Output[$key] = array(
                 'street' =>  $value[5].' '.$value[6], // Street Name
                 'zip5' =>  $value[9], // Zip Code
                 'bldg_lo_num' =>  $value[7], // House Range Begins
                 'bldg_hi_num' =>  preg_replace('/O|E|B/','', $value[8]), // House Range Ends
                 'bldg_parity' =>  partiy($value[8]), // Includes
                 'town_code' => townMap($value[1]), // Town Code
                 'town' =>  '', // Town
                 'election_code' =>  $value[3], // Electoral Dist
                 'assembly_code' =>  147, // Assembly Dist = 147 from wyoming county street file 2
                 'congressional_code' =>  27, // Congressional Dist = 27 from wyoming county street file 2
                 'senate_code' =>  59, // Senate Dist = 59 from wyoming county street file 2
                 'county_code' =>  56, // County Code
                 'state' =>  'NY' // State

             );
         }

     }

     print_r($Output);
     echo count($Output)." TOTAL\n";
 */
package gov.nysenate.sage.scripts;

import gov.nysenate.sage.boe.AddressUtils;
import gov.nysenate.sage.boe.BOEAddressRange;
import gov.nysenate.sage.boe.BOEStreetAddress;
import gov.nysenate.sage.boe.DistrictLookup;
import gov.nysenate.sage.util.DB;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.BufferedReader;
import java.io.FileReader;

import org.apache.log4j.Logger;

public class BlueBirdVoterCheck {

	public static int getInteger(String value) {
		if (value!=null && !value.trim().isEmpty()) {
			return Integer.parseInt(value);
		} else {
			return 0;
		}
	}
    public static void main(String[] args) throws Exception {
    	Logger logger = Logger.getLogger(BlueBirdVoterCheck.class);
//      File voter_file = new File("/home/stefan/street files/voterfiles/Albany County Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/CattaraugusHomeOnly.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Cayuga County Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Chautauqua County Home Only.txt");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Chenango County Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Clinton County Home Only.TXT");
      File voter_file = new File("/home/stefan/street files/voterfiles/Columbia County Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Cortland County Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Delaware County Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Dutchess County Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Erie County Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/EssexCoHome.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Franklin County Home Only.txt");
//      File voter_file = new File("/home/stefan/street files/voterfiles/FultonCoPrimary1.txt");
//      File voter_file = new File("/home/stefan/street files/voterfiles/GeneseeCoHome.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/GreenCoHomeOnly.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/HerkimerCoHome.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Jefferson County Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Lewis County Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Madison County Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Monroe County Home Only.txt");
//      File voter_file = new File("/home/stefan/street files/voterfiles/NassauCoHomeOnly.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Niagara County Home Only.txt");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Oneida County Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Onondaga County Home Only.txt");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Ontario County Home Only.txt");
//      File voter_file = new File("/home/stefan/street files/voterfiles/OrangeCoHome.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Oswego County Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/PutnamCoHome.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Rockland County Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Saratoga County Home Only.txt");
//      File voter_file = new File("/home/stefan/street files/voterfiles/SchoharieCoHomeOnly.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Seneca County Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Steuben County Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/St. Lawrence County Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/SullivanCoHome.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/TiogaCoHome.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/TompkinsCoHome.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Ulster County Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Warren County Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/WashingtonCo Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/Wayne County Home Only.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/WestchesterCo Home.TXT");
//      File voter_file = new File("/home/stefan/street files/voterfiles/WyomingCoHome.TXT");

        Pattern houseNumberPattern = Pattern.compile("([0-9]+)(?: |-)*([0-9]*)(.*)");

        DistrictLookup streetData = new DistrictLookup(DB.getDataSource());
             
        int total = 0;
        int mismatch = 0;
        int match = 0;
        int nomatch = 0;
        int multimatch = 0;
        int skipped = 0;
        int line = 1;
        String[] parts;
        String voter;
        BufferedReader br = new BufferedReader(new FileReader(voter_file));
        br.readLine(); // skipping headers
        while ((voter = br.readLine()) != null) {

            parts = voter.split("\t");
//            logger.info(voter);
            line++;  
            BOEStreetAddress address = new BOEStreetAddress();
            try {
                String last_name = parts[3];
                String first_name = parts[1];
                if(parts[2].length() > 0){
                	String middle_initial = parts[2].substring(0,1).trim();
                }
     
                String city = parts[6];
                String full_address = parts[5];
                int zip5 = getInteger(parts[8]);
                int zip4 = getInteger(parts[9]);
                String birth_date = parts[10];
                String gender = parts[11];
                int election_district = getInteger(parts[16]);
                int assembly_district = getInteger(parts[19]);
                int congress_district = getInteger(parts[17]);
                int senate_district = getInteger(parts[18]);
                int school_code = getInteger(parts[20]);
                int county_code = getInteger(parts[21]);
                String registration_date = parts[13];
                String phone_number = parts[12];
                address.town = city;
                address.zip5 = zip5;
                
                
                // The house number suffix is not always properly separated!
                Matcher house_number_matcher = houseNumberPattern.matcher(full_address);
                if (house_number_matcher.find()) {
                    address.bldg_num = Integer.parseInt(house_number_matcher.group(1) + house_number_matcher.group(2));
           
                } else {
                    System.out.println("houseNumberPattern not matched:"+full_address);
                }
                
                
                
                address = AddressUtils.parseAddress(full_address+", "+address.town+" NY "+address.zip5);

                address.state = "NY";
                address.electionCode = election_district;
                address.assemblyCode = assembly_district;
                address.congressionalCode = congress_district;
                address.senateCode = senate_district;
                address.schoolCode = String.valueOf(school_code);
                address.countyCode = county_code;
                AddressUtils.normalizeAddress(address);

                List<BOEAddressRange> results = streetData.getRangesByHouse(address);
                if (results.size() == 0) {
                    nomatch++;                    
                    System.out.println("NOMATCH: ["+line+"] "+address.bldg_num+address.bldg_chr+" "+address.street+", "+address.town+" "+address.zip5);
                } else if (results.size() > 1) {
                    multimatch++;
                    System.out.println(results.size()+" RESULTS: ["+line+"] "+address.bldg_num+address.bldg_chr+" "+address.street+", "+address.town+" "+address.zip5);
                    for (BOEAddressRange range : results) {
                        System.out.println("   "+range);
                    }
                } else {
                    BOEAddressRange range = results.get(0);
                    if (range.assemblyCode == address.assemblyCode
                            && range.senateCode == address.senateCode
                            && range.congressionalCode == address.congressionalCode
                            && range.electionCode == address.electionCode) {
                        match++;
                    } else {
                        mismatch++;
                        System.out.println("MISMATCH: ["+line+"] "+address.bldg_num+address.bldg_chr+" "+address.street+", "+address.town+" "+address.zip5);
                    }
                }
                } catch (NumberFormatException e) {
//                    System.out.println("SKIPPED: "+voter);
                    logger.error("SKIPPED: "+voter,e);
                    skipped++;
                    continue;
                }
            
            if (++total % 10000 == 0) {
                System.out.println("TOTAL: "+total+"; MATCH: "+match+"("+((match/(float)total) * 100) +"%); MISMATCH: "+mismatch+"; MULTIMATCH: "+multimatch+"; NOMATCH: "+nomatch+"; SKIPPED: "+skipped);
             }
        }        
        br.close();
        System.out.println("TOTAL: "+total+"; MATCH: "+match+"("+((match/(float)total) * 100) +"%); MISMATCH: "+mismatch+"; MULTIMATCH: "+multimatch+"; NOMATCH: "+nomatch+"; SKIPPED: "+skipped);
 
    } 
    
}

package gov.nysenate.sage.scripts;

import gov.nysenate.sage.boe.AddressUtils;
import gov.nysenate.sage.boe.BOEAddressRange;
import gov.nysenate.sage.boe.BOEStreetAddress;
import gov.nysenate.sage.boe.DistrictLookup;
import gov.nysenate.sage.util.DB;
import gov.nysenate.sage.util.Resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    	Resource config = new Resource();
    	// TODO: Make sure to add this entry to your app.properties file!
    	File base_dir = new File(config.fetch("voter_file.data"));

//      File voter_file = new File(base_dir, "Albany County Home Only.TXT");
//      File voter_file = new File(base_dir, "CattaraugusHomeOnly.TXT");
//      File voter_file = new File(base_dir, "Cayuga County Home Only.TXT");
//      File voter_file = new File(base_dir, "Chautauqua County Home Only.txt");
//      File voter_file = new File(base_dir, "Chenango County Home Only.TXT");
//      File voter_file = new File(base_dir, "Clinton County Home Only.TXT");
//      File voter_file = new File(base_dir, "Columbia County Home Only.TXT");
//      File voter_file = new File(base_dir, "Cortland County Home Only.TXT");
//      File voter_file = new File(base_dir, "Delaware County Home Only.TXT");
//      File voter_file = new File(base_dir, "Dutchess County Home Only.TXT");
//      File voter_file = new File(base_dir, "Erie County Home Only.TXT");
//      File voter_file = new File(base_dir, "EssexCoHome.TXT");
//      File voter_file = new File(base_dir, "Franklin County Home Only.txt");
//      File voter_file = new File(base_dir, "FultonCoPrimary1.txt");
//      File voter_file = new File(base_dir, "GeneseeCoHome.TXT");
      File voter_file = new File(base_dir, "GreenCoHomeOnly.TXT");
//      File voter_file = new File(base_dir, "HerkimerCoHome.TXT");
//      File voter_file = new File(base_dir, "Jefferson County Home Only.TXT");
//      File voter_file = new File(base_dir, "Lewis County Home Only.TXT");
//      File voter_file = new File(base_dir, "Madison County Home Only.TXT");
//      File voter_file = new File(base_dir, "Monroe County Home Only.txt");
//      File voter_file = new File(base_dir, "NassauCoHomeOnly.TXT");
//      File voter_file = new File(base_dir, "Niagara County Home Only.txt");
//      File voter_file = new File(base_dir, "Oneida County Home Only.TXT");
//      File voter_file = new File(base_dir, "Onondaga County Home Only.txt");
//      File voter_file = new File(base_dir, "Ontario County Home Only.txt");
//      File voter_file = new File(base_dir, "OrangeCoHome.TXT");
//      File voter_file = new File(base_dir, "Oswego County Home Only.TXT");
//      File voter_file = new File(base_dir, "PutnamCoHome.TXT");
//      File voter_file = new File(base_dir, "Rockland County Home Only.TXT");
//      File voter_file = new File(base_dir, "Saratoga County Home Only.txt");

//      // missing ed, cd, and ad .. no way to recover ?
//      File voter_file = new File(base_dir, "SchoharieCoHomeOnly.TXT");
//      File voter_file = new File(base_dir, "Seneca County Home Only.TXT");
//      File voter_file = new File(base_dir, "Steuben County Home Only.TXT");
//      File voter_file = new File(base_dir, "St. Lawrence County Home Only.TXT");
//      File voter_file = new File(base_dir, "SullivanCoHome.TXT");

//      // delete columns for  po box and address 3
//      File voter_file = new File(base_dir, "TiogaCoHome.TXT");
//      File voter_file = new File(base_dir, "TompkinsCoHome.TXT");
//      File voter_file = new File(base_dir, "Ulster County Home Only.TXT");
//      File voter_file = new File(base_dir, "Warren County Home Only.TXT");
//      File voter_file = new File(base_dir, "WashingtonCo Home Only.TXT");
//      File voter_file = new File(base_dir, "Wayne County Home Only.TXT");
//      File voter_file = new File(base_dir, "WestchesterCo Home.TXT");

//      // CD is empty , replace null with 27
//      File voter_file = new File(base_dir, "WyomingCoHome.TXT");

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

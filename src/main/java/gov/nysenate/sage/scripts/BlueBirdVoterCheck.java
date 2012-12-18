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

    public static void main(String[] args) throws Exception {
    	Logger logger = Logger.getLogger(BlueBirdVoterCheck.class);
        File voter_file = new File("/home/stefan/street files/SullivanCoPrimary1.txt");
        Pattern houseNumberPattern = Pattern.compile("([0-9]+)(?: |-)*([0-9]*)(.*)");

        DistrictLookup streetData = new DistrictLookup(DB.getDataSource());
             
        int total = 0;
        int mismatch = 0;
        int match = 0;
        int nomatch = 0;
        int multimatch = 0;
        int skipped = 0;
        
        String[] parts;
        String voter;
        BufferedReader br = new BufferedReader(new FileReader(voter_file));
        while ((voter = br.readLine()) != null) {

            parts = voter.split("\t");

            BOEStreetAddress address = new BOEStreetAddress();
            try {
//                String county_ems_id = voter.substring(0,9).trim();
                String last_name = parts[3];
                String first_name = parts[1];
                if(parts[2].length() > 0){
                	String middle_initial = parts[2].substring(0,1).trim();
                }
                // throw out apt info 
                // 
                // split building # 
                // 
//                String name_suffix = voter.substring(70,74).trim();
//                String house_number = Integer.parseInt(parts[1]);
//                String house_number_suffix = voter.substring(84,94).trim();
//                String apt_number = voter.substring(94,109).trim();
//                String street_name = voter.substring(109,159).trim();
                String city = parts[6];
                String full_address = parts[5];
                int zip5 = Integer.parseInt(parts[8]);
                int zip4 = Integer.parseInt(parts[9]);
//                String mailing_address1 = voter.substring(208,258).trim();
//                String mailing_address2 = voter.substring(258,308).trim();
//                String mailing_address3 = voter.substring(308,358).trim();
//                String mailing_address4 = voter.substring(358,408).trim();
                String birth_date = parts[10];
                String gender = parts[11];
//                String political_party = voter.substring(417,420).trim();
//                String other_party = voter.substring(420,450).trim();
                int election_district = Integer.parseInt(parts[16]);
                int assembly_district = Integer.parseInt(parts[19]);
                int congress_district = Integer.parseInt(parts[17]);
                int senate_district = Integer.parseInt(parts[18]);
                
//                String council_district = voter.substring(457,459).trim();
//                String civil_district = voter.substring(461,463).trim();
//                String judicial_district = voter.substring(463,465).trim();
                String registration_date = parts[13];
//                String status_code = voter.substring(473,475).trim();
//                String voter_type = voter.substring(475,476).trim();
//                String eff_status_change_date = voter.substring(476,484).trim();
//                String year_last_voted = voter.substring(484,488).trim();
                String phone_number = parts[12];


                address.town = city;
                address.zip5 = zip5;
                
                
                // The house number suffix is not always properly separated!
                
                Matcher house_number_matcher = houseNumberPattern.matcher(full_address);
                if (house_number_matcher.find()) {
                    address.bldg_num = Integer.parseInt(house_number_matcher.group(1) + house_number_matcher.group(2));
           
                } else {
                    System.out.println("houseNumberPattern not matched:"+full_address);
//                    address.bldg_num = Integer.parseInt(house_number);
//                    address.bldg_chr = house_number_suffix;
                }
                
                
                
                address = AddressUtils.parseAddress(full_address);
                logger.info(full_address);
                logger.info(address.bldg_num); // correctly finds number
                logger.info(address.street); // correctly finds street name
//                logger.info(address.town); // sometimes finds street type, wrong array name

                
                
                address.state = "NY";
                // address.apt_num = Integer.parseInt(apt_number);
                address.electionCode = election_district;
                address.assemblyCode = assembly_district;
                address.congressionalCode = congress_district;
                address.senateCode = senate_district;
                AddressUtils.normalizeAddress(address);
                        
//                List<BOEAddressRange> results = streetData.getRangesByHouse(address);
//                if (results.size() == 0) {
//                    nomatch++;                    
//                    System.out.println("NOMATCH: "+ address);
//                } else if (results.size() > 1) {
//                    multimatch++;
//                    System.out.println(results.size()+" RESULTS:"+address);
//                    for (BOEAddressRange range : results) {
//                        System.out.println("   "+range);
//                    }
//                } else {
//                    BOEAddressRange range = results.get(0);
//                    if (range.assemblyCode == address.assemblyCode
//                            && range.senateCode == address.senateCode
//                            && range.congressionalCode == address.congressionalCode
//                            && range.electionCode == address.electionCode) {
//                        match++;
//                    } else {
//                        mismatch++;
//                        System.out.println("MISMATCH: "+address);
//
//                    }
//                }
                } catch (NumberFormatException e) {
//                    System.out.println("SKIPPED: "+voter);
                    skipped++;
                    continue;
                }
            
            
            if (++total % 10000 == 0) {
//                System.out.println("TOTAL: "+total+"; MATCH: "+match+"("+((match/(float)total) * 100) +"%); MISMATCH: "+mismatch+"; MULTIMATCH: "+multimatch+"; NOMATCH: "+nomatch+"; SKIPPED: "+skipped);
             }
        }        
        br.close();
        
    } 
    
}

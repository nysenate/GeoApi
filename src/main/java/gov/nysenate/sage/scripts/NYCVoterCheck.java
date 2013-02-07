package gov.nysenate.sage.scripts;

import gov.nysenate.sage.boe.AddressUtils;
import gov.nysenate.sage.boe.BOEAddressRange;
import gov.nysenate.sage.boe.BOEStreetAddress;
import gov.nysenate.sage.boe.DistrictLookup;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.DB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NYCVoterCheck {

    public static void main(String[] args) throws Exception {
    	File base_dir = new File(Config.read("voter_file.data"));
//    	File voter_file = new File(base_dir, "Brooklyn-all.txt");
    	File voter_file = new File(base_dir, "Bronx-All.txt");
//		File voter_file = new File(base_dir, "Manhattan-All.txt");
//		File voter_file = new File(base_dir, "Queens-All.txt");
//    	File voter_file = new File(base_dir, "Staten Island-All.txt");

        Pattern houseNumberPattern = Pattern.compile("([0-9]+)(?: |-)*([0-9]*)(.*)");

        DistrictLookup streetData = new DistrictLookup(ApplicationFactory.getDataSource());

        int total = 0;
        int mismatch = 0;
        int match = 0;
        int nomatch = 0;
        int multimatch = 0;
        int skipped = 0;

        String voter;
        BufferedReader br = new BufferedReader(new FileReader(voter_file));
        while ((voter = br.readLine()) != null) {
            BOEStreetAddress address = new BOEStreetAddress();
            try {
                String county_ems_id = voter.substring(0,9).trim();
                String last_name = voter.substring(9,39).trim();
                String first_name = voter.substring(39,69).trim();
                String middle_initial = voter.substring(69,70).trim();
                String name_suffix = voter.substring(70,74).trim();
                String house_number = voter.substring(74,84).trim();
                String house_number_suffix = voter.substring(84,94).trim();
                String apt_number = voter.substring(94,109).trim();
                String street_name = voter.substring(109,159).trim();
                String city = voter.substring(159,199).trim();
                int zip5 = Integer.parseInt(voter.substring(199,204).trim());
                String zip4 = voter.substring(204,208).trim();
                String mailing_address1 = voter.substring(208,258).trim();
                String mailing_address2 = voter.substring(258,308).trim();
                String mailing_address3 = voter.substring(308,358).trim();
                String mailing_address4 = voter.substring(358,408).trim();
                String birth_date = voter.substring(408,416).trim();
                String gender = voter.substring(416,417).trim();
                String political_party = voter.substring(417,420).trim();
                String other_party = voter.substring(420,450).trim();
                int election_district = Integer.parseInt(voter.substring(450,453).trim());
                int assembly_district = Integer.parseInt(voter.substring(453,455).trim());
                int congress_district = Integer.parseInt(voter.substring(455,457).trim());
                String council_district = voter.substring(457,459).trim();
                int senate_district = Integer.parseInt(voter.substring(459,461).trim());
                String civil_district = voter.substring(461,463).trim();
                String judicial_district = voter.substring(463,465).trim();
                String registration_date = voter.substring(465,473).trim();
                String status_code = voter.substring(473,475).trim();
                String voter_type = voter.substring(475,476).trim();
                String eff_status_change_date = voter.substring(476,484).trim();
                String year_last_voted = voter.substring(484,488).trim();
                String phone_number = voter.substring(488,500).trim();


                address.town = city;
                address.zip5 = zip5;
                // The house number suffix is not always properly separated!
                Matcher house_number_matcher = houseNumberPattern.matcher(house_number);
                if (house_number_matcher.find()) {
                    address.bldg_num = Integer.parseInt(house_number_matcher.group(1) + house_number_matcher.group(2));
                    if (house_number_suffix == "") {
                        address.bldg_chr = house_number_matcher.group(3);
                    } else {
                        address.bldg_chr = house_number_suffix;
                    }
                } else {
                    System.out.println("houseNumberPattern not matched:"+house_number);
                    address.bldg_num = Integer.parseInt(house_number);
                    address.bldg_chr = house_number_suffix;
                }

                address.street = street_name;
                address.state = "NY";
                // address.apt_num = Integer.parseInt(apt_number);
                address.electionCode = election_district;
                address.assemblyCode = assembly_district;
                address.congressionalCode = congress_district;
                address.senateCode = senate_district;
                AddressUtils.normalizeAddress(address);

                List<BOEAddressRange> results = streetData.getRangesByHouse(address);
                if (results.size() == 0) {
                    nomatch++;
                    System.out.println("NOMATCH: "+ address);
                } else if (results.size() > 1) {
                    multimatch++;
                    System.out.println(results.size()+" RESULTS:"+address);
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
                        System.out.println("MISMATCH: "+address);

                    }
                }
                } catch (NumberFormatException e) {
                    System.out.println("SKIPPED: "+voter);
                    skipped++;
                    continue;
                }


            if (++total % 10000 == 0) {
                System.out.println("TOTAL: "+total+"; MATCH: "+match+"("+((match/(float)total) * 100) +"%); MISMATCH: "+mismatch+"; MULTIMATCH: "+multimatch+"; NOMATCH: "+nomatch+"; SKIPPED: "+skipped);
             }
        }
        br.close();
        System.out.println("FINAL TOTAL: "+total+"; MATCH: "+match+"("+((match/(float)total) * 100) +"%); MISMATCH: "+mismatch+"; MULTIMATCH: "+multimatch+"; NOMATCH: "+nomatch+"; SKIPPED: "+skipped);

    }

}

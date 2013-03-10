package gov.nysenate.sage.boe;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.service.DistrictService;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.log4j.Logger;

@Deprecated
public class  DistrictLookup {
    public QueryRunner runner;
    public ResultSetHandler<List<BOEAddressRange>> rangeHandler;
    public final Logger logger = Logger.getLogger(DistrictLookup.class);
    public boolean DEBUG = false;

    public DistrictLookup(DataSource db) throws Exception {
        runner = new QueryRunner(db);
        Map<String, String> column_map = new HashMap<String, String>();
        column_map.put("congressional_code", "congressionalCode");
        column_map.put("senate_code", "senateCode");
        column_map.put("election_code","electionCode");
        column_map.put("county_code","countyCode");
        column_map.put("assembly_code","assemblyCode");
        column_map.put("bldg_lo_num","bldgLoNum");
        column_map.put("bldg_hi_num","bldgHiNum");
        column_map.put("bldg_parity","bldgParity");
        column_map.put("apt_lo_num", "aptLoNum");
        column_map.put("apt_hi_num", "aptHiNum");
        column_map.put("apt_parity","aptParity");
        column_map.put("town_code","townCode");
        column_map.put("senate_town_code", "senateTownCode");
        column_map.put("ward_code", "wardCode");
        column_map.put("cc_code", "ccCode");
        column_map.put("school_code", "schoolCode");
        column_map.put("senate_school_code", "senateSchoolCode");
        column_map.put("cleg_code","clegCode");
        column_map.put("fire_code","fireCode");
        column_map.put("city_code","cityCode");
        column_map.put("vill_code","villCode");
        BeanProcessor rowProcessor = new BeanProcessor(column_map);
        rangeHandler = new BeanListHandler<BOEAddressRange>(BOEAddressRange.class, new BasicRowProcessor(rowProcessor));
    }

    protected List<BOEAddressRange> getRanges(BOEStreetAddress address, boolean useStreet, boolean fuzzy, boolean useHouse) throws SQLException {
        ArrayList<Object> params = new ArrayList<Object>();
        String sql = "SELECT * \n"
                + "FROM street_data \n"
                + "WHERE 1=1 \n";

        boolean whereZip = (address.zip5 != 0);
        boolean whereState = (address.state != null && !address.state.isEmpty());
        boolean whereStreet = (useStreet && address.street != null && !address.street.isEmpty());
        boolean whereBldg = (useHouse && address.bldg_num != 0);
        boolean whereBldgChr = (useHouse && address.bldg_chr != null && !address.bldg_chr.isEmpty());

        if (whereZip) {
            sql += "  AND zip5=? \n";
            params.add(address.zip5);
        }

        if (whereState) {
            sql += "  AND state=? \n";
            params.add(address.state);
        }

        if (whereStreet) {

            // Sometimes the bldg_chr is actually the tail end of the street name
            if (whereBldgChr) {
                // Handle dashed NYC buildings by collapsing on the dash
                if (address.bldg_chr.startsWith("-"))  {
                    try {
                        address.bldg_num = Integer.parseInt(String.valueOf(address.bldg_num)+address.bldg_chr.substring(1));
                        address.bldg_chr = null;
                    } catch (NumberFormatException e) {
                        logger.warn("bldg_chr `"+address.bldg_chr+"` not as expected.");
                    }
                }

                // Every one else gets a range check; sometimes the suffix is actually part of the street prefix.
                if (address.bldg_chr != null) {
                    if (fuzzy) {
                        sql += "  AND (street LIKE ? OR (street LIKE ? AND (bldg_lo_chr='' OR bldg_lo_chr <= ?) AND (bldg_hi_chr='' OR ? <= bldg_hi_chr))) \n";
                        params.add(address.bldg_chr+" "+address.street+"%");
                        params.add(address.street+"%");
                    } else {
                        sql += "  AND (street = ? OR (street = ? AND (bldg_lo_chr='' OR bldg_lo_chr <= ?) AND (bldg_hi_chr='' OR ? <= bldg_hi_chr))) \n";
                        params.add(address.bldg_chr+" "+address.street);
                        params.add(address.street);
                    }
                    params.add(address.bldg_chr);
                    params.add(address.bldg_chr);
                }

            } else {
                if (fuzzy) {
                    sql += "  AND (street LIKE ?) \n";
                    params.add(address.street+"%");
                } else {
                    sql += "  AND (street = ?) \n";
                    params.add(address.street);
                }
            }

            if (whereBldg) {
                sql += "  AND (bldg_lo_num <= ? AND ? <= bldg_hi_num AND (bldg_parity='ALL' or bldg_parity=? )) \n";
                params.add(address.bldg_num);
                params.add(address.bldg_num);
                params.add((address.bldg_num % 2 == 0 ? "EVENS" : "ODDS"));
            }
        }

        // Only do a lookup if we have meaningful filters on the query
        if (whereZip || whereStreet) {
            if (DEBUG) {
                System.out.println(sql);
                for (Object o : params) {
                    System.out.println(o);
                }
            }

            return runner.query(sql, rangeHandler, params.toArray());
        } else {
            if (DEBUG) {
                System.out.println("Skipping address: no identifying information");
            }
            return new ArrayList<BOEAddressRange>();
        }
    }

    public List<BOEAddressRange> getRangesByZip(BOEStreetAddress address) throws SQLException {
        return getRanges(address, false, false, false);
    }

    public List<BOEAddressRange> getRangesByHouse(BOEStreetAddress address) throws SQLException {
        List<BOEAddressRange> ranges = getRanges(address, true, false, true);
        if (ranges.size()==0) {
            return getRanges(address, true, true, true);
        } else {
            return ranges;
        }
    }

    public List<BOEAddressRange> getRangesByStreet(BOEStreetAddress address) throws SQLException {
        List<BOEAddressRange> ranges = getRanges(address, true, false, false);
        if (ranges.size()==0) {
            return getRanges(address, true, true, false);
        } else {
            return ranges;
        }
    }

    /** Duplicated from AddressUtils. Method from AddressUtils will be removed. */
    public static BOEAddressRange consolidateRanges(List<BOEAddressRange> ranges) {
        if (ranges.size() == 0) return null;

        BOEAddressRange base = ranges.get(0);
        int sd = base.senateCode;
        int ed = base.electionCode;
        int cong = base.congressionalCode;
        int ad = base.assemblyCode;
        int county = base.countyCode;
        String school = base.schoolCode;
        String senateSchool = base.senateSchoolCode;
        int ward = base.wardCode;
        String townCode = base.townCode;
        String senateTownCode = base.senateTownCode;
        int ccCode = base.ccCode;
        int cleg = base.clegCode;

        for (int i=1; i < ranges.size(); i++) {
            BOEAddressRange range = ranges.get(i);
            // State wide
            if (range.senateCode != sd) { sd = 0; }
            if (range.congressionalCode != cong) { cong = 0; }
            if (range.assemblyCode != ad) { ad = 0; }
            if (range.countyCode != county) { county = 0; }

            // County Specific
            if (range.schoolCode ==null || !range.schoolCode.equals(school) || range.countyCode!=base.countyCode) { school = ""; }
            if (range.senateSchoolCode == null || !range.senateSchoolCode.equals(senateSchool) || range.countyCode != base.countyCode) { senateSchool = ""; }
            if (range.townCode ==null || !range.townCode.equals(townCode) || range.countyCode!=base.countyCode) { townCode= ""; }
            if (range.senateTownCode == null || !range.senateTownCode.equals(senateTownCode) || range.countyCode != base.countyCode) { senateTownCode= ""; }
            if (range.clegCode != cleg || range.countyCode!=base.countyCode) { cleg = 0; }

            // Town specific
            if (range.wardCode != ward || range.countyCode!=base.countyCode || !range.town.equals(base.town)) { ward = 0; }
            if (range.ccCode != ccCode || range.countyCode!=base.countyCode || !range.town.equals(base.town)) { ccCode = 0; }

            // Ward Specific (maybe)
            if (range.electionCode != ed || range.countyCode!=base.countyCode || !range.town.equals(base.town) || range.wardCode != base.wardCode) { ed = 0; }
        }

        if (sd != 0) {
            BOEAddressRange range = new BOEAddressRange();
            range.senateCode = sd;
            range.assemblyCode = ad;
            range.electionCode = ed;
            range.congressionalCode = cong;
            range.schoolCode = school;
            range.senateSchoolCode = senateSchool;
            range.wardCode = ward;
            range.clegCode = cleg;
            range.ccCode = ccCode;
            range.townCode = townCode;
            range.senateTownCode = senateTownCode;
            range.countyCode = county;
            range.state = base.state;
            range.street = base.street;
            range.zip5 = base.zip5;
            return range;

        } else {
            return null;
        }
    }

    /**
     * The street lookup process deals primarily with the BOEAddressRange object. Once the lookup is complete
     * the found districts need to be mapped back to the original Address object from which the range was created
     * from. The following function will map those districts to the Address object and return the district types
     * that could not be mapped due to missing data. The missing types information can be utilized in instances
     * where fallback services might be used to ensure all district information have been found.
     *
     * @param address       The Address object to set district information to.
     * @param range         The BOEAddressRange object that contains the district information.
     * @param types         An ArrayList containing the desired district types to store.
     * @return              An ArrayList containing the district types that did not have mappings.
     */
    public static ArrayList<DistrictService.TYPE> setDistrictsForAddress(Address address, BOEAddressRange range, List<DistrictService.TYPE> types) {

        ArrayList<DistrictService.TYPE> missingTypes = new ArrayList<>(types);
        if (range == null || address == null) {
            return null;
        }
        if (types.contains(DistrictService.TYPE.ASSEMBLY)) {
            if (isValidDistrictCode(range.assemblyCode)){
                address.assembly_code = range.assemblyCode;
                missingTypes.remove(DistrictService.TYPE.ASSEMBLY);
            }
        }
        if (types.contains(DistrictService.TYPE.SENATE)) {
            if (isValidDistrictCode(range.senateCode)){
                address.senate_code = range.senateCode;
                missingTypes.remove(DistrictService.TYPE.SENATE);
            }
        }
        if (types.contains(DistrictService.TYPE.CONGRESSIONAL)) {
            if (isValidDistrictCode(range.congressionalCode)){
                address.congressional_code = range.congressionalCode;
                missingTypes.remove(DistrictService.TYPE.CONGRESSIONAL);
            }
        }
        if (types.contains(DistrictService.TYPE.COUNTY)) {
            if (isValidDistrictCode(range.countyCode)){
                address.county_code = range.countyCode;
                missingTypes.remove(DistrictService.TYPE.COUNTY);
            }
        }
        if (types.contains(DistrictService.TYPE.ELECTION)) {
            if (isValidDistrictCode(range.electionCode)){
                address.election_code = range.electionCode;
                missingTypes.remove(DistrictService.TYPE.ELECTION);
            }
        }
        if (types.contains(DistrictService.TYPE.SCHOOL)) {
            if (isValidDistrictCode(range.senateSchoolCode)){
                address.school_code = range.senateSchoolCode;
                missingTypes.remove(DistrictService.TYPE.SCHOOL);
            }
        }
        if (types.contains(DistrictService.TYPE.TOWN)) {
            if (isValidDistrictCode(range.senateTownCode)){
                address.town_code = range.senateTownCode;
                missingTypes.remove(DistrictService.TYPE.TOWN);
            }
        }

        /** These aren't set as distict types yet.. */
        address.cleg_code = range.clegCode;
        address.ward_code = range.wardCode;

        return (ArrayList) missingTypes;
    }

    /** Valid named districts are not empty, null, or zeroes */
    protected static boolean isValidDistrictCode(String code){
        return (code != null && !code.isEmpty() && !code.equals("NULL")
                && !code.equals("0") && !code.equals("00") && !code.equals("000"));
    }

    /** Valid numerical districts are greater than zero */
    protected static boolean isValidDistrictCode(int code){
        return (code > 0);
    }
}

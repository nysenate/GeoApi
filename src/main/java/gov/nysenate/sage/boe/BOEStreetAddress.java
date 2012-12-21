package gov.nysenate.sage.boe;

public class BOEStreetAddress extends BOEAddress {
    public int bldg_num;
    public String bldg_chr;

    public int apt_num;
    public String apt_chr;

    @Override
    public String toString() {
        return ((bldg_num!=0 ? bldg_num : "")+(bldg_chr!=null ? bldg_chr : "")+" "+street+" "+((apt_num!=0) ? apt_num : "")+((apt_chr!=null) ? apt_chr : "")+", "+town+" "+state+" "+zip5).replaceAll("\\s+"," ").trim();
    }
}

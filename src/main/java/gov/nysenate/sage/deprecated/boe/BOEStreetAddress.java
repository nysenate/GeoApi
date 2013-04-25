package gov.nysenate.sage.deprecated.boe;

@Deprecated
public class BOEStreetAddress extends BOEAddress {

    public int bldg_num;
    public String bldg_chr;

    public int apt_num;
    public String apt_chr;

    public BOEStreetAddress() {}

    public BOEStreetAddress(int bldg_num, String bldg_chr, String street, String town, int zip5){
        this.bldg_num = bldg_num;
        this.bldg_chr = bldg_chr;
        this.street = street;
        this.town = town;
        this.zip5 = zip5;
    }

    @Override
    public String toString() {
        return ((bldg_num!=0 ? bldg_num : "")+(bldg_chr!=null ? bldg_chr : "")+" "+street+" "+((apt_num!=0) ? apt_num : "")+((apt_chr!=null) ? apt_chr : "")+", "+town+" "+state+" "+zip5).replaceAll("\\s+"," ").trim();
    }

}

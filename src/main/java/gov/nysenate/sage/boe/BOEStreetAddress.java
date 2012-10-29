package gov.nysenate.sage.boe;

public class BOEStreetAddress extends BOEAddress {
    public int bldg_num;
    public String bldg_chr;

    public int apt_num;
    public String apt_chr;

    @Override
    public String toString() {
        return bldg_num+bldg_chr+" "+street+" "+apt_num+apt_chr+", "+town+" "+state+" "+zip5;
    }
}

package gov.nysenate.sage.model.districts;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@Deprecated
@XStreamAlias("county")
public class County {
    String countyName;
    String district;

    public County() {

    }

    public County(String countyName) {
        this.countyName = countyName;
    }

    public County(String countyName, String district) {
        this.countyName = countyName;
        this.district = district;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }
}

package gov.nysenate.sage.model.districts;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@Deprecated
@XStreamAlias("school")
public class School {
    String district;

    public School() {

    }

    public School(String district) {
        this.district = district;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }
}

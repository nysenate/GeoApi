package gov.nysenate.sage.model.district;

public class County
{
    private int id;
    private String name;
    private int fipsCode;
    private String link;

    public County() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFipsCode() {
        return fipsCode;
    }

    public void setFipsCode(int fipsCode) {
        this.fipsCode = fipsCode;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}

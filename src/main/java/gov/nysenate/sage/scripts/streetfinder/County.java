package gov.nysenate.sage.scripts.streetfinder;

public class County {

    private String name;
    private String id;

    public County() {}

    public County(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String toString() {
        return name.toUpperCase() + "\t" + id + "\n";
    }
}

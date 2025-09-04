package gov.nysenate.sage.model.district;

public record County(int senateCode, int voterfileCode, String name, String link, String streetfileName) {
    public boolean inNYC() {
        return senateCode >= 60;
    }
}

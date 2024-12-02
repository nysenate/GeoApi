package gov.nysenate.sage.model.district;

public record ElectionOverlap(int townCityId, int assemblyId, int senateId, int countyFips, int congressionalId) {
    @Override
    public String toString() {
        return "ElectionOverlap{" +
                "townCityId=" + townCityId +
                ", assemblyId=" + assemblyId +
                ", senateId=" + senateId +
                ", countyFips=" + countyFips +
                ", congressionalId=" + congressionalId +
                '}';
    }
}

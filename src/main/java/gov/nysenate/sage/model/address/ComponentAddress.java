package gov.nysenate.sage.model.address;

public class ComponentAddress extends Address
{
    protected int streetNumber;
    protected int buildingNumber;
    protected String buildingChar;
    protected int apartmentNumber;
    protected int apartmentChar;

    public ComponentAddress(){}

    public int getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(int streetNumber) {
        this.streetNumber = streetNumber;
    }

    public int getBuildingNumber() {
        return buildingNumber;
    }

    public void setBuildingNumber(int buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public String getBuildingChar() {
        return buildingChar;
    }

    public void setBuildingChar(String buildingChar) {
        this.buildingChar = buildingChar;
    }

    public int getApartmentNumber() {
        return apartmentNumber;
    }

    public void setApartmentNumber(int apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
    }

    public int getApartmentChar() {
        return apartmentChar;
    }

    public void setApartmentChar(int apartmentChar) {
        this.apartmentChar = apartmentChar;
    }
}

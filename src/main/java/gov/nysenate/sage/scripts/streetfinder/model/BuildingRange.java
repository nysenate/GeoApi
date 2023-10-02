package gov.nysenate.sage.scripts.streetfinder.model;

import gov.nysenate.sage.util.Pair;
import gov.nysenate.sage.util.Tuple;

import java.util.Arrays;
import java.util.List;

public class BuildingRange {
    private String lowBuilding = "", highBuilding = "";
    private StreetParity parity = StreetParity.ALL;

    public Pair<String> range() {
        return new Pair<>(lowBuilding, highBuilding);
    }

    public boolean isSingleton() {
        return lowBuilding.equals(highBuilding);
    }

    public String getBuilding(boolean isLow) {
        return isLow ? lowBuilding : highBuilding;
    }
    
    public void setHigh(String highData) {
        this.highBuilding = highData;
    }

    public void setLow(String lowData) {
        this.lowBuilding = lowData;
    }

    public void setParity(String parity) {
        this.parity = StreetParity.getParityFromWord(parity);
    }

    public void setParity() {
        this.parity = StreetParity.getParityFromRange(
                getBuildingTuple(lowBuilding).first(),
                getBuildingTuple(highBuilding).first());
    }

    public List<Object> getData() {
        Tuple<Integer, String> lowTuple = getBuildingTuple(lowBuilding);
        Tuple<Integer, String> highTuple = getBuildingTuple(highBuilding);
        return Arrays.asList(new Object[] {lowTuple.first(), lowTuple.second(),
                highTuple.first(), highTuple.second(), parity.name()});
    }

    private Tuple<Integer, String> getBuildingTuple(String data) {
        String digits = data.replaceFirst("(?i)[A-Z]+", "");
        String characters = data.replaceFirst("\\d+", "");
        if (!digits.matches("\\d*") || !characters.matches("(?i)[A-Z]*")) {
            throw new IllegalArgumentException("Data could not be pulled from: " + data);
        }
        Integer parsedInt = digits.isEmpty() ? null : Integer.parseInt(digits);
        return new Tuple<>(parsedInt, characters);
    }
}

package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

// TODO: what are MC and CO?
public enum Column {
    FROM((streetFinderAddress, s) -> streetFinderAddress.setBuildingLow(StreetFinderAddress.cleanBuilding(s))),
    TO((streetFinderAddress, s) -> streetFinderAddress.setBuildingHigh(StreetFinderAddress.cleanBuilding(s))),
    ED(StreetFinderAddress::setED), AD(StreetFinderAddress::setAsm),
    ZIP(StreetFinderAddress::setStreet), CD(StreetFinderAddress::setCong), SD(StreetFinderAddress::setSen),
    MC(), CO();

    private final BiConsumer<StreetFinderAddress, String> sfaMethod;

    Column(BiConsumer<StreetFinderAddress, String> sfaMethod) {
        this.sfaMethod = sfaMethod;
    }

    // We don't do anything with the data in some columns.
    Column() {
        this.sfaMethod = (streetFinderAddress, s) -> {};
    }

    public static boolean handleDataPoints(StreetFinderAddress sfa, String[] points) {
        ArrayList<Column> columns = new ArrayList<>();
        if (points.length < 6 || points.length > 9) {
            return false;
        }
        if (points.length == 6) {
            columns.addAll(List.of(ED, AD));
        }
        else {
            if (points.length >= 8) {
                columns.add(FROM);
            }
            if (points.length == 9) {
                columns.add(TO);
            }
            columns.addAll(List.of(ED, AD, ZIP));
        }
        columns.addAll(List.of(CD, SD, MC, CO));

        for (int i = 0; i < points.length; i++) {
            columns.get(i).sfaMethod.accept(sfa, points[i]);
        }
        return true;
    }
}

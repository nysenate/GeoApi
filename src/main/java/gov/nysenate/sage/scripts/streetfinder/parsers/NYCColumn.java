package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.model.address.StreetFileField.*;

// TODO: what are MC and CO?
// TODO: really NYC columns
enum NYCColumn {
    FROM((streetFinderAddress, s) -> streetFinderAddress.setBuilding(true, StreetFinderAddress.cleanBuilding(s))),
    TO((streetFinderAddress, s) -> streetFinderAddress.setBuilding(false, StreetFinderAddress.cleanBuilding(s))),
    ED(StreetFinderAddress::setED), AD((streetFinderAddress, s) -> streetFinderAddress.put(ASSEMBLY, s)),
    ZIP(StreetFinderAddress::setStreet), CD((streetFinderAddress, s) -> streetFinderAddress.put(CONGRESSIONAL, s)),
    SD((streetFinderAddress, s) -> streetFinderAddress.put(SENATE, s)), MC(), CO();

    private final BiConsumer<StreetFinderAddress, String> sfaMethod;

    NYCColumn(BiConsumer<StreetFinderAddress, String> sfaMethod) {
        this.sfaMethod = sfaMethod;
    }

    // We don't do anything with the data in some columns.
    NYCColumn() {
        this.sfaMethod = (streetFinderAddress, s) -> {};
    }

    public static boolean handleDataPoints(StreetFinderAddress sfa, String[] points) {
        if (points.length < 6 || points.length > 9) {
            return false;
        }
        ArrayList<NYCColumn> columns = new ArrayList<>();
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

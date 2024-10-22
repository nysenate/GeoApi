package gov.nysenate.sage.dao.provider.tiger;

import gov.nysenate.sage.model.geo.Line;

import java.util.List;

public interface TigerDao {
    List<Line> getStreetLineGeometry(String streetName, List<Integer> zip5List);
}

package gov.nysenate.sage.model.api;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.geo.Point;

import java.util.List;

public class BatchDistrictRequest extends DistrictRequest
{
    private List<Address> addresses;
    private List<Point> points;
}

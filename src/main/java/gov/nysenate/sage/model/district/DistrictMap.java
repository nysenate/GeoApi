package gov.nysenate.sage.model.district;

import gov.nysenate.sage.model.geo.Polygon;
import gov.nysenate.services.model.Senator;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds all applicable information for a given district map which includes
 * the geometry, member info, district code, etc.
 */
public class DistrictMap
{
    protected DistrictType districtType;
    protected String districtCode;
    protected String districtName;
    private List<Polygon> polygons = new ArrayList<>();

    protected Senator senator;
    protected DistrictMember member;

    public DistrictMap() {}

    public DistrictType getDistrictType() {
        return districtType;
    }

    public void setDistrictType(DistrictType districtType) {
        this.districtType = districtType;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public DistrictMap(List<Polygon> polygons) {
        this.polygons = polygons;
    }

    public List<Polygon> getPolygons() {
        return polygons;
    }

    public void setPolygons(List<Polygon> polygons) {
        this.polygons = polygons;
    }

    public void addPolygon(Polygon polygon) {
        this.polygons.add(polygon);
    }

    public Senator getSenator() {
        return senator;
    }

    public void setSenator(Senator senator) {
        this.senator = senator;
    }

    public DistrictMember getMember() {
        return member;
    }

    public void setMember(DistrictMember member) {
        this.member = member;
    }

    public String toString()
    {
        String o = "";
        if (polygons != null) {
            for (Polygon polygon : polygons) {
                o += polygon.toString();
            }
        }
        return o;
    }
}

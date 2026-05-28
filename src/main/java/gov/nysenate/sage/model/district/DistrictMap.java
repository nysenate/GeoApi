package gov.nysenate.sage.model.district;

import gov.nysenate.sage.model.geo.Polygon;
import gov.nysenate.services.model.Senator;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Contains district map geometry information.
 */
public class DistrictMap implements Comparable<DistrictMap> {
    private final DistrictType districtType;
    private final String districtCode;
    private final String districtName;
    private Senator senator;
    private DistrictMember member;
    // Only used for County maps.
    private String link;
    // TownCity maps also need to know the base name.
    private String baseName;
    private final List<Polygon> polygons = new ArrayList<>();
    private String geometryType = "";
    // Note that this is only an approximation.
    private BigDecimal area;

    public DistrictMap(DistrictType type, String name, String code) {
        this.districtType = type;
        this.districtName = name;
        this.districtCode = code;
    }

    public DistrictType getDistrictType() {
        return districtType;
    }

    public String getDistrictName() {
        return districtName;
    }

    public String getDistrictCode() {
        return districtCode;
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

    public List<Polygon> getPolygons() {
        return polygons;
    }

    public void addPolygon(Polygon polygon) {
        polygons.add(polygon);
    }

    public String getGeometryType() {
        return geometryType;
    }

    public void setGeometryType(String geometryType) {
        this.geometryType = geometryType;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    @Override
    public String toString() {
        var o = new StringBuilder();
        for (Polygon polygon : polygons) {
            o.append(polygon.toString());
        }
        return o.toString();
    }

    @Override
    public int compareTo(@Nonnull DistrictMap o) {
        if (districtType == DistrictType.TOWN_CITY) {
            int result = baseName.compareTo(o.baseName);
            if (result != 0) {
                return result;
            }
        }
        int i = Arrays.mismatch(districtName.toCharArray(), o.districtName.toCharArray());
        if (i < 0) {
            return 0;
        }
        // It's common for names to have a common form, e.g. District x, where x is the code.
        // These should be sorted by the code.
        String a = districtName.substring(i), b = o.districtName.substring(i);
        try {
            return Long.compare(Long.parseLong(a), Long.parseLong(b));
        } catch (NumberFormatException ex) {
            return a.compareTo(b);
        }
    }
}

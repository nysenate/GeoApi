package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.view.district.BaseDistrictView;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.provider.district.ShapefileService;
import gov.nysenate.sage.service.district.DistrictCodeCache;
import gov.nysenate.sage.service.district.DistrictMemberProvider;
import gov.nysenate.sage.service.district.DistrictNameCache;
import gov.nysenate.sage.util.HasDisplayName;

public abstract class BaseDistrictController<E extends Enum<E> & HasDisplayName> extends SourcedController<E> {
    protected final DistrictCodeCache<String> nameCache;
    protected final DistrictCodeCache<DistrictMap> mapCache;
    protected final DistrictCodeCache<DistrictMember> memberCache;

    protected BaseDistrictController(DistrictNameCache nameCache, ShapefileService shapefileService,
                                     DistrictMemberProvider memberProvider) {
        this.nameCache = nameCache;
        this.mapCache = shapefileService.getMapCache();
        this.memberCache = memberProvider.getMemberCache();
    }

    protected void assignData(BaseDistrictView bdv, boolean showMembers, boolean showMaps) {
        if (bdv == null) {
            return;
        }
        bdv.setName(nameCache.getData(bdv.getType(), bdv.getDistrict()));
        if (showMembers) {
            bdv.setMember(memberCache.getData(bdv.getType(), bdv.getDistrict()));
        }
        if (showMaps) {
            DistrictMap map = mapCache.getData(bdv.getType(), bdv.getDistrict());
            if (map != null) {
                bdv.setMap(map.getMapGeoJson());
            }
        }
    }
}

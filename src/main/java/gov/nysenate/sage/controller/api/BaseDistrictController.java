package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.view.district.BaseDistrictView;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.provider.district.ShapefileService;
import gov.nysenate.sage.service.district.DistrictIdCache;
import gov.nysenate.sage.service.district.DistrictMemberProvider;
import gov.nysenate.sage.service.district.DistrictInfoCache;
import gov.nysenate.sage.util.HasDisplayName;

public abstract class BaseDistrictController<E extends Enum<E> & HasDisplayName> extends SourcedController<E> {
    protected final DistrictInfoCache infoCache;
    protected final DistrictIdCache<DistrictMap> mapCache;
    protected final DistrictIdCache<DistrictMember> memberCache;

    protected BaseDistrictController(DistrictInfoCache infoCache, ShapefileService shapefileService,
                                     DistrictMemberProvider memberProvider) {
        this.infoCache = infoCache;
        this.mapCache = shapefileService.getMapCache();
        this.memberCache = memberProvider.getMemberCache();
    }

    protected void assignData(BaseDistrictView bdv, boolean showMembers, boolean showMaps) {
        if (bdv == null) {
            return;
        }
        DistrictInfo currInfo = infoCache.getData(bdv.getType(), bdv.getId());
        if (currInfo != null) {
            bdv.setName(currInfo.getName());
        }
        bdv.setDistrict(infoCache.getCode(bdv.getType(), bdv.getId()));
        if (showMembers) {
            bdv.setMember(memberCache.getData(bdv.getType(), bdv.getId()));
        }
        if (showMaps) {
            DistrictMap map = mapCache.getData(bdv.getType(), bdv.getId());
            if (map != null) {
                bdv.setMap(map.getMapGeoJson());
            }
        }
    }
}

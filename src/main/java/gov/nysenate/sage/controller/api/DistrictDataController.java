package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.view.district.BaseDistrictView;
import gov.nysenate.sage.dao.provider.DistrictNameDao;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.provider.district.ShapefileService;
import gov.nysenate.sage.service.district.DistrictCodeCache;
import gov.nysenate.sage.service.district.DistrictMemberProvider;
import gov.nysenate.sage.util.HasDisplayName;
import org.springframework.stereotype.Controller;

@Controller
public abstract class DistrictDataController<E extends Enum<E> & HasDisplayName> extends SourcedController<E> {
    private final DistrictCodeCache<String> nameService;
    private final DistrictCodeCache<DistrictMap> mapService;
    private final DistrictCodeCache<DistrictMember> memberService;

    protected DistrictDataController(DistrictNameDao nameDao, ShapefileService shapefileService,
                                     DistrictMemberProvider memberProvider) {
        this.nameService = new DistrictCodeCache<>(nameDao::getNameMap);
        this.mapService = shapefileService.getMapCache();
        this.memberService = memberProvider.getMemberCache();
    }

    protected void assignData(BaseDistrictView bdv, boolean showMembers, boolean showMaps) {
        if (bdv == null) {
            return;
        }
        bdv.setName(nameService.getData(bdv.getType(), bdv.getDistrict()));
        if (showMembers) {
            bdv.setMember(memberService.getData(bdv.getType(), bdv.getDistrict()));
        }
        if (showMaps) {
            DistrictMap map = mapService.getData(bdv.getType(), bdv.getDistrict());
            if (map != null) {
                bdv.setMap(map.getMapGeoJson());
            }
        }
    }
}

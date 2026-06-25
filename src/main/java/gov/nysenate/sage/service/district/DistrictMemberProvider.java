package gov.nysenate.sage.service.district;

import com.google.common.collect.ImmutableMap;
import gov.nysenate.sage.dao.model.member.MemberDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.district.*;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.DistrictResultWithMembers;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.GeocodeService;
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.util.AssemblyScraper;
import gov.nysenate.sage.util.HouseScraper;
import gov.nysenate.services.NYSenateJSONClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Typically when the district service providers return a DistrictInfo, only the district codes
 * and maps are provided. This class provides methods to populate the remaining data which includes
 * the district members and the senator information. Since this information is not always required, this
 * functionality should be invoked through a controller as opposed to the provider implementations.
 */
@Component
public class DistrictMemberProvider {
    private static final Logger logger = LoggerFactory.getLogger(DistrictMemberProvider.class);
    private static final Address LOB = new Address("198 State St", "Albany", "NY", "12247");

    private final MemberDao memberDao;
    private final AddressService addressService;
    private final GeocodeService geocodeService;
    private final EnumMap<DistrictType, ImmutableMap<Long, DistrictMember>> caches = new EnumMap<>(DistrictType.class);
    @Value("${nysenate.domain:https://www.nysenate.gov}")
    private String nysenateDomain;

    @Autowired
    public DistrictMemberProvider(MemberDao memberDao, AddressService addressService, GeocodeService geocodeService) {
        this.memberDao = memberDao;
        this.addressService = addressService;
        this.geocodeService = geocodeService;
        Arrays.stream(DistrictType.values()).forEach(type ->
                {
                    Map<Long, DistrictMember> memberMap = memberDao.getMembers(type);
                    if (memberMap != null) {
                        caches.put(type, ImmutableMap.copyOf(memberMap));
                    }
                }
        );
    }

    public void updateDistrictMembers(DistrictType type) throws IOException {
        Map<Long, DistrictMember> newMemberMap = switch (type) {
            case SENATE -> new NYSenateJSONClient(nysenateDomain).getSenators().stream().collect(
                    Collectors.toMap(senator -> Long.valueOf(senator.getDistrict().getNumber()),
                            senator -> new DistrictMember(senator, senator.getOffices()))
            );
            case ASSEMBLY -> AssemblyScraper.getAssemblyMembers();
            case CONGRESSIONAL -> HouseScraper.getHouseMembers();
            default -> Map.of();
        };
        if (newMemberMap.isEmpty()) {
            throw new RuntimeException("No %s members found!".formatted(type));
        }

        for (var entry :  newMemberMap.entrySet()) {
            if (entry.getValue().offices() == null) {
                continue;
            }
            for (OfficeInfo info : entry.getValue().offices()) {
                if (info.getPoint() != null) {
                    continue;
                }
                Address addressToGeocode = info.getAddress().getRealAddress();
                if (addressToGeocode.getAddr1().matches("(\\d+ )?Legislative Office (Bldg|Building).*")) {
                    addressToGeocode = LOB;
                }
                // Offices within the Capitol are corrected poorly by AMS, since it's a unique zipcode.
                // TODO: improve?
                if (!"12247".equals(info.getAddress().zip5())) {
                    addressToGeocode = addressService.validateOrDefault(addressToGeocode);
                }
                info.setPoint(getPoint(addressToGeocode));
            }
        }

        memberDao.refreshMemberData(type, newMemberMap);
        caches.put(type, ImmutableMap.copyOf(memberDao.getMembers(type)));
    }

    private Point getPoint(Address officeAddress) {
        GeocodeResult result = geocodeService.geocode(null, officeAddress, false);
        if (result.isSuccess()) {
            return result.getGeocode().point();
        }
        else {
            logger.error("Unable to geocode this office address: {}", officeAddress);
            return null;
        }
    }

    /**
     * Adds the senator, congressional, and/or assembly member data to the map result.
     */
    public void assignMember(DistrictMap map) {
        if (map == null) {
            return;
        }
        map.setMember(getMember(map.getDistrictCode(), map.getDistrictType()));
    }

    public DistrictResultWithMembers assignMembers(DistrictResult baseResult) {
        var memberMap = new HashMap<DistrictType, DistrictMember>();
        for (DistrictType type : baseResult.getAssignedDistricts()) {
            DistrictMember member = getMember(baseResult.getDistrictInfo().getDistCode(type), type);
            if (member != null) {
                memberMap.put(type, member);
            }
        }
        return new DistrictResultWithMembers(baseResult, memberMap);
    }

    private DistrictMember getMember(String codeStr, DistrictType type) {
        ImmutableMap<Long, DistrictMember> cache = caches.get(type);
        if (cache == null) {
            return null;
        }
        DistrictMember member = cache.get(Long.parseLong(codeStr));
        if (member == null && type == DistrictType.SENATE) {
            member = new DistrictMember(new MemberInfo("Vacant", "District " + codeStr,
                    "https://www.nysenate.gov/themes/custom/nysenate_theme/dist/images/nys_logo_header240x240.jpg",
                    "https://www.nysenate.gov/district/" + codeStr, null), null);
        }
        return member;
    }
}

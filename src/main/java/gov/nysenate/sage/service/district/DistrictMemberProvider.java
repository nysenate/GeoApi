package gov.nysenate.sage.service.district;

import gov.nysenate.sage.dao.model.member.MemberDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.district.*;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.GeocodeService;
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.util.AssemblyScraper;
import gov.nysenate.sage.util.HouseScraper;
import gov.nysenate.services.NYSenateJSONClient;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Typically when the district service providers return a DistrictInfo, only the district codes
 * and maps are provided. This class provides methods to populate the remaining data which includes
 * the district members and the senator information. Since this information is not always required, this
 * functionality should be invoked through a controller as opposed to the provider implementations.
 */
@Service
public class DistrictMemberProvider {
    private static final Logger logger = LoggerFactory.getLogger(DistrictMemberProvider.class);
    private static final Point pointForLOB = new Point("42.65284900371907", "-73.75931474712434");

    private final MemberDao memberDao;
    private final AddressService addressService;
    private final GeocodeService geocodeService;
    @Getter
    private final DistrictMemberCache memberCache = new DistrictMemberCache(this::getMemberMap);
    @Value("${nysenate.domain:https://www.nysenate.gov}")
    private String nysenateDomain;

    @Autowired
    public DistrictMemberProvider(MemberDao memberDao, AddressService addressService, GeocodeService geocodeService) {
        this.memberDao = memberDao;
        this.addressService = addressService;
        this.geocodeService = geocodeService;
    }

    private Map<DistrictId, DistrictMember> getMemberMap(DistrictType type) {
        Map<Long, DistrictMember> longToMemberMap = memberDao.getMembers(type);
        if (longToMemberMap == null) {
            return null;
        }
        return longToMemberMap.entrySet().stream()
                .collect(Collectors.toMap(entry -> new DistrictId(entry.getKey().toString()), Map.Entry::getValue));
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
                info.setPoint(getPoint(info.getAddress().getRealAddress()));
            }
        }

        memberDao.refreshMemberData(type, newMemberMap);
        memberCache.refresh();
    }

    private Point getPoint(Address officeAddress) {
        // Passing a validated LOB address into the geocoders doesn't work well.
        if (officeAddress.toString().matches(".*(LOB |Legislative Office (Building|Bldg)).*")) {
            return pointForLOB;
        }
        Address validatedAddress = addressService.validateOrDefault(officeAddress);
        GeocodeResult result = geocodeService.geocode(null, validatedAddress, false);
        if (result.isSuccess()) {
            return result.getGeocode().point();
        }
        else {
            logger.error("Unable to geocode this office address: {}", officeAddress);
            return null;
        }
    }
}

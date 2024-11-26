package gov.nysenate.sage.service.district;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.api.BatchDistrictRequest;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.BaseResult;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.district.DistrictService;
import gov.nysenate.sage.provider.district.DistrictShapefile;
import gov.nysenate.sage.provider.district.DistrictSource;
import gov.nysenate.sage.provider.district.Streetfile;
import gov.nysenate.sage.service.PostOfficeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gov.nysenate.sage.provider.district.DistrictSource.SHAPEFILE;
import static gov.nysenate.sage.provider.district.DistrictSource.STREETFILE;

/**
 * Point of access for all district assignment requests. This class maintains a collection of available
 * district providers and contains logic for distributing requests and collecting responses from the providers.
 */
@Service
public class DistrictServiceProvider implements SageDistrictServiceProvider {
    private final Map<DistrictSource, DistrictService> providers = new HashMap<>();
    private final PostOfficeService postOfficeService;

    @Autowired
    public DistrictServiceProvider(DistrictShapefile districtShapefile, Streetfile streetFile,
                                   PostOfficeService postOfficeService) {
        this.postOfficeService = postOfficeService;
        providers.put(SHAPEFILE, districtShapefile);
        providers.put(STREETFILE, streetFile);
    }

    /**
     * If a district provider is specified use that for district assignment.
     * Otherwise, the default strategy for district assignment is to run both street file and district shape file
     * look-ups in parallel. Once results from both lookup methods are retrieved they are compared and consolidated.
     */
    public DistrictResult assignDistricts(final GeocodedAddress geocodedAddress, final List<DistrictSource> distProviders,
                                          final List<DistrictType> districtTypes) {
        DistrictResult districtResult = consolidateDistrictResults(distProviders.stream()
                .map(provider -> providers.get(provider).assignDistricts(geocodedAddress, districtTypes))
                .toList());
        fixPostOfficeBoxResult(districtResult);
        districtResult.setResultTime();
        return districtResult;
    }

    /**
     * Assign standard districts with options set in BatchDistrictRequest.
     */
    public List<DistrictResult> assignDistricts(final BatchDistrictRequest bdr) {
        List<List<DistrictResult>> batches = new ArrayList<>();
        for (DistrictSource provider : bdr.getProviders()) {
            batches.add(providers.get(provider).assignDistricts(bdr.getGeocodedAddresses(), DistrictType.getStandardTypes()));
        }
        // Ensures the batch results are all the same size.
        if (batches.stream().map(List::size).distinct().count() != 1) {
            throw new IllegalStateException("District result sizes must match.");
        }
        var consolidatedResults = new ArrayList<DistrictResult>();
        for (int i = 0; i < batches.get(0).size(); i++) {
            int finalI = i;
            List<DistrictResult> resultsToConsolidate = batches.stream().map(batch -> batch.get(finalI)).toList();
            consolidatedResults.add(consolidateDistrictResults(resultsToConsolidate));
        }
        consolidatedResults.forEach(this::fixPostOfficeBoxResult);
        consolidatedResults.forEach(BaseResult::setResultTime);
        return consolidatedResults;
    }

    private void fixPostOfficeBoxResult(DistrictResult result) {
        Address currAddr = result.getAddress();
        if (currAddr != null && currAddr.isPOBox()) {
            DistrictedAddress poResult = postOfficeService.getDistrictedAddress(currAddr.getZip5(), currAddr.getPostalCity());
            if (poResult != null) {
                result.setStatusCode(ResultStatus.SUCCESS);
                result.setDistrictedAddress(poResult);
            }
        }
    }

    /**
     * Perform result consolidation.
     * @return Consolidated district result
     */
    private static DistrictResult consolidateDistrictResults(List<DistrictResult> results) {
        List<DistrictResult> validResults = results.stream().filter(BaseResult::isSuccess).toList();
        if (validResults.isEmpty()) {
            return results.get(0);
        }
        var finalInfo = validResults.get(0).getDistrictInfo();
        for (int i = 1; i < validResults.size(); i++) {
            for (DistrictType districtType : validResults.get(i).getAssignedDistricts()) {
                if (finalInfo.getDistCode(districtType) == null) {
                    DistrictInfo currInfo = validResults.get(i).getDistrictInfo();
                    finalInfo.setDistCode(districtType, currInfo.getDistCode(districtType));
                    finalInfo.setDistName(districtType, currInfo.getDistName(districtType));
                }
            }
        }
        validResults.get(0).getDistrictedAddress().setDistrictInfo(finalInfo);
        return validResults.get(0);
    }
}

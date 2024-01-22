package gov.nysenate.sage.service.district;

import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.api.BatchDistrictRequest;
import gov.nysenate.sage.model.api.DistrictRequest;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;

import java.util.List;

public interface SageDistrictServiceProvider {

    /**
     * Assign districts using supplied DistrictRequest.
     * @param geocodedAddress
     * @param districtRequest
     * @return DistrictResult
     */
    public DistrictResult assignDistricts(final GeocodedAddress geocodedAddress, final DistrictRequest districtRequest);

    /**
     * Assign standard districts using default method.
     * @param geocodedAddress
     * @return DistrictResult
     */
    public DistrictResult assignDistricts(final GeocodedAddress geocodedAddress);

    /**
     * Assign standard districts using specified provider
     * @param geocodedAddress
     * @param distProvider
     * @return DistrictResult
     */
    public DistrictResult assignDistricts(final GeocodedAddress geocodedAddress, final String distProvider);

    /**
     * If a district provider is specified use that for district assignment.
     * Otherwise the default strategy for district assignment is to run both street file and district shape file
     * look-ups in parallel. Once results from both lookup methods are retrieved they are compared and consolidated.
     *
     * @param geocodedAddress
     * @param distProvider
     * @return
     */
    public DistrictResult assignDistricts(final GeocodedAddress geocodedAddress, final String distProvider,
                                          final List<DistrictType> districtTypes,
                                          DistrictServiceProvider.DistrictStrategy districtStrategy);

    /**
     * Assign standard districts with options set in BatchDistrictRequest.
     * @param bdr
     * @return
     */
    public List<DistrictResult> assignDistricts(final BatchDistrictRequest bdr);

    /**
     * Assign specified district types using default method.
     * @param geocodedAddresses
     * @param districtTypes
     * @return List<DistrictResult>
     */
    public List<DistrictResult> assignDistricts(final List<GeocodedAddress> geocodedAddresses,
                                                final List<DistrictType> districtTypes);

    /**
     * Assign specified district types using an assortment of district strategies.
     * @param geocodedAddresses
     * @param distProvider  If district provider is specified, (e.g streetfile), then only that provider will be used.
     * @param districtTypes
     * @return List<DistrictResult>
     */
    public List<DistrictResult> assignDistricts(final List<GeocodedAddress> geocodedAddresses, final String distProvider,
                                                final List<DistrictType> districtTypes,
                                                DistrictServiceProvider.DistrictStrategy districtStrategy);

    /**
     * Assigns a Geocoded address to multiple districts
     * @param geocodedAddress
     * @param zipProvided
     * @return
     */
    public DistrictResult assignMultiMatchDistricts(GeocodedAddress geocodedAddress, boolean zipProvided);


}

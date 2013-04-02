package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.ApiError;
import gov.nysenate.sage.client.response.DistrictResponse;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.base.ServiceProviders;
import gov.nysenate.sage.service.district.DistrictService;
import gov.nysenate.sage.service.district.DistrictServiceMetadata;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.*;

import static gov.nysenate.sage.model.result.ResultStatus.*;

public class DistrictController extends BaseApiController
{
    private static Logger logger = Logger.getLogger(DistrictController.class);
    private static ServiceProviders<DistrictService> districtProviders = ApplicationFactory.getDistrictServiceProviders();
    private static GeocodeServiceProvider geocodeServiceProvider = ApplicationFactory.getGeocodeServiceProvider();

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        logger.debug("Initialized " + this.getClass().getSimpleName());
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        this.doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Object districtResponse;

        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);
        String provider = apiRequest.getProvider();

        /** Allow for specifying which geocoder to use */
        String geoProvider = request.getParameter("geoProvider");

        /** Fetch senator and other member info if true */
        Boolean showMembers = Boolean.parseBoolean(request.getParameter("showMembers"));

        /** Specify whether or not to return map data */
        Boolean showMaps = Boolean.parseBoolean(request.getParameter("showMaps"));

        /**
         * If providers are specified then make sure they match the available providers. Send an
         * api error and return if the provider is not supported.
         */
        if (provider != null && !provider.isEmpty() && !districtProviders.isRegistered(provider)) {
            setApiResponse(new ApiError(this.getClass(), DISTRICT_PROVIDER_NOT_SUPPORTED), request);
            return;
        }
        if (geoProvider != null && !geoProvider.isEmpty() && !geocodeServiceProvider.isRegistered(geoProvider)) {
            setApiResponse(new ApiError(this.getClass(), GEOCODE_PROVIDER_NOT_SUPPORTED), request);
            return;
        }

        /** Handle single request */
        if (!apiRequest.isBatch()) {
            Address address = getAddressFromParams(request);
            switch (apiRequest.getRequest()) {
                case "assign": {
                    if (address != null && !address.isEmpty()) {
                        districtResponse = new DistrictResponse(assignDistricts(address, geoProvider, provider, showMembers, showMaps));
                    }
                    else {
                        districtResponse = new ApiError(this.getClass(), MISSING_ADDRESS);
                    }
                    break;
                }
                default : {
                    districtResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
                }
            }
        }
        /** Handle batch request */
        else {
            districtResponse = new ApiError(this.getClass(), FEATURE_NOT_SUPPORTED);
        }

        setApiResponse(districtResponse, request);
    }

    /**
     * If a district provider is specified use that for district assignment.
     * Otherwise the default strategy for district assignment is to run both street file and district shape file
     * lookups in parallel. The geocoding routine declared in GeocodeController is used for retrieving the geocode.
     * Once results from both lookup methods are retrieved they are compared and consolidated. If both services
     * return results and the senate districts are different then throw an api error. Otherwise give preference
     * to the shape file lookup for other district mismatches.
     *
     * Note: the arguments are declared as final to allow them to be used in anonymous callable routines.
     * @param address
     * @param geoProvider
     * @param distProvider
     * @param showMembers
     * @param showMaps
     * @return
     */
    public static DistrictResult assignDistricts(final Address address, final String geoProvider, final String distProvider,
                                                 final boolean showMembers, final boolean showMaps)
    {
        logger.info(address);

        DistrictResult districtResult = null;
        ExecutorService executorService = null;

        try {
            /** Use the provider if specified */
            if (distProvider != null && !distProvider.isEmpty()) {
                executorService = Executors.newSingleThreadExecutor();
                Future<DistrictResult> future = executorService.submit(getDistrictsCallable(address, distProvider, geoProvider, showMaps));
                districtResult = future.get();
            }
            else {
                Callable<DistrictResult> streetfileCallable = getDistrictsCallable(address, "streetfile", null, false);
                Callable<DistrictResult> shapefileCallable = getDistrictsCallable(address, "shapefile", geoProvider, showMaps);

                executorService = Executors.newFixedThreadPool(2);
                Future<DistrictResult> streetfileFuture = executorService.submit(streetfileCallable);
                Future<DistrictResult> shapefileFuture = executorService.submit(shapefileCallable);

                DistrictResult streetfileResult = streetfileFuture.get();
                DistrictResult shapefileResult = shapefileFuture.get();

                districtResult = shapefileResult;
            }
        }
        catch (InterruptedException ex) {
            logger.error("Failed to get district results from future!", ex);
        }
        catch (ExecutionException ex) {
            logger.error("Failed to get district results from future!", ex);
        }
        finally {
            executorService.shutdownNow();
        }

        if (showMembers) {
            DistrictServiceMetadata.assignDistrictMembers(districtResult);
        }

        // logger.debug(FormatUtil.toJsonString(districtResult));

        return districtResult;
    }

    private static Callable<DistrictResult> getDistrictsCallable(final Address address, final String distProvider,
                                                                 final String geoProvider, final boolean showMaps)
    {
        return new Callable<DistrictResult>() {
            @Override
            public DistrictResult call() throws Exception {
                DistrictResult districtResult = new DistrictResult(DistrictController.class);
                DistrictService districtService = districtProviders.newInstance(distProvider);
                Geocode geocode = null;

                /** Use GeocodeController to geocode if necessary */
                if (districtService.requiresGeocode()) {
                    GeocodeResult geocodeResult = (geoProvider != null) ? geocodeServiceProvider.geocode(address, geoProvider, false) : geocodeServiceProvider.geocode(address);
                    geocode = (geocodeResult != null && geocodeResult.isSuccess()) ? geocodeResult.getGeocode() : null;

                    /** Proceed with district assignment only if geocode was successful */
                    if (geocode == null) {
                        districtResult.setStatusCode(NO_GEOCODE_RESULT);
                        return districtResult;
                    }
                }

                /** Tell the service if it should get map data or not */
                districtService.fetchMaps(showMaps);

                /** Retrieve all standard district types */
                return districtService.assignDistricts(new GeocodedAddress(address, geocode), DistrictType.getStandardTypes());
            }
        };
    }
}
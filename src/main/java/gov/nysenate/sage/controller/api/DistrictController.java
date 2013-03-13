package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.api.ApiError;
import gov.nysenate.sage.client.api.district.DistrictResponse;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.ServiceProviders;
import gov.nysenate.sage.service.district.DistrictService;
import gov.nysenate.sage.service.geo.GeocodeService;
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
    private Logger logger = Logger.getLogger(DistrictController.class);
    private ServiceProviders<DistrictService> districtProviders;
    private ServiceProviders<GeocodeService> geocodeProviders;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        districtProviders = ApplicationFactory.getDistrictServiceProviders();
        geocodeProviders = ApplicationFactory.getGeoCodeServiceProviders();
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
        String geoProvider = request.getParameter("geoProvider"); // Allow for specifying which geocoder to use

        /**
         * If providers are specified then make sure they match the available providers. Send an
         * api error and return if the provider is not supported.
         */
        if (provider != null && !provider.isEmpty() && !districtProviders.isRegistered(provider)) {
            setApiResponse(new ApiError(this.getClass(), DISTRICT_PROVIDER_NOT_SUPPORTED), request);
            return;
        }
        if (geoProvider != null && !geoProvider.isEmpty() && !geocodeProviders.isRegistered(geoProvider)) {
            setApiResponse(new ApiError(this.getClass(), GEOCODE_PROVIDER_NOT_SUPPORTED), request);
            return;
        }

        /** Handle single request */
        if (!apiRequest.isBatch()) {
            Address address = getAddressFromParams(request);
            switch (apiRequest.getRequest()) {
                case "assign": {
                    if (address != null && !address.isEmpty()) {
                        districtResponse = new DistrictResponse(assignDistricts(address, geoProvider, provider));
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
     * @param address
     * @param geoProvider
     * @param distProvider
     * @return
     */
    public DistrictResult assignDistricts(final Address address, String geoProvider, String distProvider)
    {
        Callable<DistrictResult> streetfileCallable = getAssignDistrictsCallable(address, "streetfile", false, null);
        Callable<DistrictResult> shapefileCallable = getAssignDistrictsCallable(address, "shapefile", true, geoProvider);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<DistrictResult> streetfileFuture = executorService.submit (streetfileCallable);
        Future<DistrictResult> shapefileFuture = executorService.submit(shapefileCallable);

        try {
            DistrictResult districtResult = new DistrictResult();
            DistrictResult streetfileResult = streetfileFuture.get(3, TimeUnit.SECONDS);
            DistrictResult shapefileResult = shapefileFuture.get(3, TimeUnit.SECONDS);

            FormatUtil.printObject(streetfileResult);
            FormatUtil.printObject(shapefileResult);
        }
        catch (Exception ex){
            logger.error("Failed to get district results from future!", ex);
        }
        finally {
            executorService.shutdownNow();
        }
        return null;
    }

    private Callable<DistrictResult> getAssignDistrictsCallable(final Address address, final String distProvider,
                                                                final boolean geocodeRequired, final String geoProvider)
    {
        return new Callable<DistrictResult>() {
            @Override
            public DistrictResult call() throws Exception {
                DistrictService districtService = districtProviders.newInstance(distProvider);
                GeocodeResult geocodeResult = (geocodeRequired) ?  GeocodeController.geocode(address, geoProvider) : null;
                Geocode geocode = (geocodeResult != null && geocodeResult.isSuccess()) ? geocodeResult.getGeocode() : null;
                return districtService.assignDistricts(new GeocodedAddress(address, geocode));
            }
        };
    }
}
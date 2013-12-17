package gov.nysenate.sage.client.response.meta;

import gov.nysenate.sage.client.view.meta.ActiveGeocoderView;
import gov.nysenate.sage.service.geo.GeocodeService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MetaProviderResponse
{
    private List<ActiveGeocoderView> activeGeocoders;

    public MetaProviderResponse(Map<String, Class<? extends GeocodeService>> activeGeocoderMap)
    {
        if (activeGeocoderMap != null) {
            activeGeocoders = new ArrayList<>();
            for (String shortName : activeGeocoderMap.keySet()) {
                activeGeocoders.add(new ActiveGeocoderView(shortName, activeGeocoderMap.get(shortName)));
            }
        }
    }

    public List<ActiveGeocoderView> getActiveGeocoders() {
        return activeGeocoders;
    }
}

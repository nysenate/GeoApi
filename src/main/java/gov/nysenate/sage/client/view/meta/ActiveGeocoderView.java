package gov.nysenate.sage.client.view.meta;

import gov.nysenate.sage.provider.geocode.GeocodeService;

public class ActiveGeocoderView
{
    private String name;
    private String shortName;

    public ActiveGeocoderView (String shortName, Class<? extends GeocodeService> geocoderClass)
    {
        if (geocoderClass != null) {
            this.shortName = shortName;
            this.name = geocoderClass.getSimpleName();
        }
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }
}

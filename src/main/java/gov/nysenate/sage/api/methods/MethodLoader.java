package gov.nysenate.sage.api.methods;

import gov.nysenate.sage.model.ApiMethod;
import gov.nysenate.sage.model.ErrorResponse;
import gov.nysenate.sage.model.Point;
import gov.nysenate.sage.model.ValidateResponse;
import gov.nysenate.sage.model.districts.DistrictResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MethodLoader {
	@SuppressWarnings("unchecked")
	public static HashMap<String, ApiMethod> getMethods() {
		HashMap<String, ApiMethod> methods = null;

		methods = new HashMap<String, ApiMethod>();

		ApiMethod method = new ApiMethod("geocode", GeoCodeMethod.class, false,
				new ArrayList<String>(Arrays.asList("addr", "extended", "bulk")),
				new ArrayList<String>(Arrays.asList("csv", "json", "xml")),
				new ArrayList<Class<? extends Object>>(Arrays.asList(Point.class)));

		methods.put("geocode", method);

		method = new ApiMethod("revgeo", RevGeoMethod.class, true,
				new ArrayList<String>(Arrays.asList("latlon")),
				new ArrayList<String>(Arrays.asList("json", "xml")),
				new ArrayList<Class<? extends Object>>(Arrays.asList(Point.class)));

		methods.put("revgeo", method);

		method = new ApiMethod("districts", DistrictsMethod.class, true,
				new ArrayList<String>(Arrays.asList("addr", "extended","latlon")), 
				new ArrayList<String>(Arrays.asList("json","xml")), 
				new ArrayList<Class<? extends Object>>(Arrays.asList(
						DistrictResponse.class, Point.class)));

		methods.put("districts", method);

		method = new ApiMethod("validate", ValidateMethod.class, true,
				new ArrayList<String>(Arrays.asList("extended")),
				new ArrayList<String>(Arrays.asList("json", "xml")),
				new ArrayList<Class<? extends Object>>(Arrays.asList(
				ValidateResponse.class, ErrorResponse.class)));

		methods.put("validate", method);

		method = new ApiMethod("poly", PolyMethod.class, true,
				new ArrayList<String>(Arrays.asList("senate", "assembly","congressional")), 
				new ArrayList<String>(Arrays.asList("json", "kml", "xml")), 
				null);

		methods.put("poly", method);

		method = new ApiMethod("polysearch", PolySearchMethod.class, true,
				new ArrayList<String>(Arrays.asList("senate", "assembly","congressional", "election", "county")),
				new ArrayList<String>(Arrays.asList("json", "kml", "xml")),
				null);

		methods.put("polysearch", method);

		method = new ApiMethod("citystatelookup", CityStateLookupMethod.class,
				true, new ArrayList<String>(Arrays.asList("extended")),
				new ArrayList<String>(Arrays.asList("json", "xml")),
				new ArrayList<Class<? extends Object>>(Arrays.asList(
						ValidateResponse.class, ErrorResponse.class)));

		methods.put("citystatelookup", method);

		method = new ApiMethod("zipcodelookup", ZipCodeLookupMethod.class,
				true, new ArrayList<String>(Arrays.asList("extended")),
				new ArrayList<String>(Arrays.asList("json", "xml")),
				new ArrayList<Class<? extends Object>>(Arrays.asList(
						ValidateResponse.class, ErrorResponse.class)));

		methods.put("zipcodelookup", method);
		
		method = new ApiMethod("streetlookup", StreetLookupMethod.class,
				true, new ArrayList<String>(Arrays.asList("zip")),
				new ArrayList<String>(Arrays.asList("json", "xml")),
				new ArrayList<Class<? extends Object>>());

		methods.put("streetlookup", method);
		
		return methods;
	}
}

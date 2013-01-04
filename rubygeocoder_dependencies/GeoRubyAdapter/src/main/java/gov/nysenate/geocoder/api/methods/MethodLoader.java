package gov.nysenate.geocoder.api.methods;

import gov.nysenate.geocoder.model.ApiMethod;

import java.util.HashMap;

public class MethodLoader {
	
	public static HashMap<String, ApiMethod> getMethods() {
		HashMap<String, ApiMethod> methods = null;

		methods = new HashMap<String, ApiMethod>();
				
		ApiMethod method = new ApiMethod("geocode", GeocodeMethod.class, false,
				null,
				null,
				null);
		methods.put("geocode", method);
		
		method = new ApiMethod("bulk", BulkMethod.class, false,
				null,
				null,
				null);
		methods.put("bulk", method);
		
		return methods;
	}
}

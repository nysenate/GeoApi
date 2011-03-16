package gov.nysenate.sage.api.methods;

import java.util.EnumSet;
import java.util.HashMap;

public enum RequestCodes {
	FORMAT(0), TYPE(1), ADDRESS(2), ZIP(2), LATLON(2), DISTRICT(2), POLY(1), POLY_TYPE(2), POLY_ADDRESS(3), POLY_LATLON(3);

	private static final HashMap<Integer, RequestCodes> lookup = new HashMap<Integer, RequestCodes>();

	static {
		for (RequestCodes s : EnumSet.allOf(RequestCodes.class))
			lookup.put(s.code(), s);
	}

	private int code;

	private RequestCodes(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}

	public static RequestCodes get(int code) {
		return lookup.get(code);
	}
};
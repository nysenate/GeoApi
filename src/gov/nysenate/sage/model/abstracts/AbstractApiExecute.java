package gov.nysenate.sage.model.abstracts;

import gov.nysenate.sage.api.exceptions.ApiException;
import gov.nysenate.sage.api.exceptions.ApiFormatException;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract interface AbstractApiExecute {
	public Object execute(HttpServletRequest request, HttpServletResponse response, ArrayList<String> strings) throws ApiException;
	public String toXml(Object obj, ArrayList<Class<?>> xstreamClasses);
	public String toJson(Object obj);
	public String toOther(Object obj, String format) throws ApiFormatException;
}

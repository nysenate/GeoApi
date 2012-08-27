package gov.nysenate.sage.model;

import java.util.ArrayList;

public class ApiMethod {

	public String methodName;
	public ApiExecution executor;
	public boolean writeMetric;
	public ArrayList<String> inputTypes;
	public ArrayList<String> outputFormats;
	public ArrayList<Class<?>> xstreamClasses;

	public ApiMethod() {
		this(null,null,false,null,null,null);
	}

	public ApiMethod(String methodName, ApiExecution executor,
			boolean writeMetric, ArrayList<String> inputTypes, ArrayList<String> outputFormats,
			ArrayList<Class<?>> xstreamClasses) {
		this.methodName = (methodName == null) ? "" : methodName;
		this.executor = (executor == null) ? new ApiExecution() : executor;
		this.writeMetric = writeMetric;
		this.inputTypes = (inputTypes == null) ? new ArrayList<String>() : inputTypes;
		this.outputFormats = (outputFormats == null) ? new ArrayList<String>() : outputFormats;
		this.xstreamClasses = (xstreamClasses == null) ? new ArrayList<Class<?>>() : xstreamClasses;
	}
}

package gov.nysenate.sage.model;

import java.util.ArrayList;

public class ApiMethod {
	
	String methodName;
	Class<? extends ApiExecution> executionClass;
	boolean writeMetric;
	ArrayList<String> inputTypes;
	ArrayList<String> outputFormats;
	ArrayList<Class<?>> xstreamClasses;
	
	public ApiMethod() {
		initialize();
	}
	
	public ApiMethod(String methodName, Class<? extends ApiExecution> executionClass,
			boolean writeMetric, ArrayList<String> inputTypes, ArrayList<String> outputFormats,
			ArrayList<Class<?>> xstreamClasses) {
		this.methodName = methodName;
		this.executionClass = executionClass;
		this.writeMetric = writeMetric;
		this.inputTypes = inputTypes;
		this.outputFormats = outputFormats;
		this.xstreamClasses = xstreamClasses;
		initialize();
	}
	
	private void initialize() {
		if(methodName == null)
			methodName = "";
		if(executionClass ==  null)
			executionClass = ApiExecution.class;
		if(inputTypes == null)
			inputTypes = new ArrayList<String>();
		if(outputFormats == null)
			outputFormats = new ArrayList<String>();
		if(xstreamClasses == null)
			xstreamClasses = new ArrayList<Class<?>>();
	}

	public String getMethodName() {
		return methodName;
	}
	
	public Class<? extends ApiExecution> getExecutionClass() {
		return executionClass;
	}
	
	public boolean getWriteMetric() {
		return writeMetric;
	}

	public ArrayList<String> getInputTypes() {
		return inputTypes;
	}

	public ArrayList<String> getOutputFormats() {
		return outputFormats;
	}

	public ArrayList<Class<?>> getXstreamClasses() {
		return xstreamClasses;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public void setExecutionClass(Class<? extends ApiExecution> executionClass) {
		this.executionClass = executionClass;
	}
	
	public void setWriteMetric(boolean writeMetric) {
		this.writeMetric = writeMetric;
	}
	
	public void setInputTypes(ArrayList<String> inputTypes) {
		this.inputTypes = inputTypes;
	}

	public void setOutputFormats(ArrayList<String> outputFormats) {
		this.outputFormats = outputFormats;
	}

	public void setXstreamClasses(ArrayList<Class<?>> xstreamClasses) {
		this.xstreamClasses = xstreamClasses;
	}
	
	public boolean validType(String inputType) {
		return inputTypes.contains(inputType);
	}
	
	public boolean validFormat(String outputFormat) {
		return outputFormats.contains(outputFormat);
	}
	
	public ApiExecution getInstanceOfExecutionClass() {
		try {
			return executionClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}

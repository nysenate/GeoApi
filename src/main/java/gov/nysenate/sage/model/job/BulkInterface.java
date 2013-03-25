package gov.nysenate.sage.model.job;

public interface BulkInterface {

	public void setSD(String sd);
	public void setCD(String cd);
	public void setAD(String ad);
	public void setED(String ed);
	public void setCounty(String county);
	public void setTown(String town);
	public void setSchool(String school);
	public void setLat(String lat);
	public void setLon(String lon);
	public String getAddress();
	public String getStreet();
	public String getCity();
	public String getState();
	public String getZip5();

}

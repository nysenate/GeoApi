package gov.nysenate.sage.model.job;

public class PublicWebsiteTSV {
	String Serial;
	String SID;
	String Time;
	String Draft;
	String IPAddress;
	String UID;
	String Username;
	String YourName;
	String YourAddress;
	String CityTown1;
	String ZipCode1;
	String YourTelephoneNumber;
	String YourEmailAddress;
	String NameofVeteran;
	String BranchofMilitaryVeteranServedin;
	String WhyareyounominatingthisVeterantobeamemberoftheNewYorkStateSenateVeteranHallofFame;
	String StreetAddressofVeteran;
	String CityTown2;
	String ZipCode2;
	String Name;
	String FilesizeKB;
	
	public PublicWebsiteTSV() {
		Serial="";
		SID="";
		Time="";
		Draft="";
		IPAddress="";
		UID="";
		Username="";
		YourName="";
		YourAddress="";
		CityTown1="";
		ZipCode1="";
		YourTelephoneNumber="";
		YourEmailAddress="";
		NameofVeteran="";
		BranchofMilitaryVeteranServedin="";
		WhyareyounominatingthisVeterantobeamemberoftheNewYorkStateSenateVeteranHallofFame="";
		StreetAddressofVeteran="";
		CityTown2="";
		ZipCode2="";
		Name="";
		FilesizeKB="";
	}

	public String getSerial() {
		return Serial;
	}

	public String getSID() {
		return SID;
	}

	public String getTime() {
		return Time;
	}

	public String getDraft() {
		return Draft;
	}

	public String getIPAddress() {
		return IPAddress;
	}

	public String getUID() {
		return UID;
	}

	public String getUsername() {
		return Username;
	}

	public String getYourName() {
		return YourName;
	}

	public String getYourAddress() {
		return YourAddress;
	}

	public String getCityTown1() {
		return CityTown1;
	}

	public String getZipCode1() {
		return ZipCode1;
	}

	public String getYourTelephoneNumber() {
		return YourTelephoneNumber;
	}

	public String getYourEmailAddress() {
		return YourEmailAddress;
	}

	public String getNameofVeteran() {
		return NameofVeteran;
	}

	public String getBranchofMilitaryVeteranServedin() {
		return BranchofMilitaryVeteranServedin;
	}

	public String getWhyareyounominatingthisVeterantobeamemberoftheNewYorkStateSenateVeteranHallofFame() {
		return WhyareyounominatingthisVeterantobeamemberoftheNewYorkStateSenateVeteranHallofFame;
	}

	public String getStreetAddressofVeteran() {
		return StreetAddressofVeteran;
	}

	public String getCityTown2() {
		return CityTown2;
	}

	public String getZipCode2() {
		return ZipCode2;
	}

	public String getName() {
		return Name;
	}

	public String getFilesizeKB() {
		return FilesizeKB;
	}

	public void setSerial(String serial) {
		Serial = serial;
	}

	public void setSID(String sID) {
		SID = sID;
	}

	public void setTime(String time) {
		Time = time;
	}

	public void setDraft(String draft) {
		Draft = draft;
	}

	public void setIPAddress(String iPAddress) {
		IPAddress = iPAddress;
	}

	public void setUID(String uID) {
		UID = uID;
	}

	public void setUsername(String username) {
		Username = username;
	}

	public void setYourName(String yourName) {
		YourName = yourName;
	}

	public void setYourAddress(String yourAddress) {
		YourAddress = yourAddress;
	}

	public void setCityTown1(String cityTown1) {
		CityTown1 = cityTown1;
	}

	public void setZipCode1(String zipCode1) {
		ZipCode1 = zipCode1;
	}

	public void setYourTelephoneNumber(String yourTelephoneNumber) {
		YourTelephoneNumber = yourTelephoneNumber;
	}

	public void setYourEmailAddress(String yourEmailAddress) {
		YourEmailAddress = yourEmailAddress;
	}

	public void setNameofVeteran(String nameofVeteran) {
		NameofVeteran = nameofVeteran;
	}

	public void setBranchofMilitaryVeteranServedin(
			String branchofMilitaryVeteranServedin) {
		BranchofMilitaryVeteranServedin = branchofMilitaryVeteranServedin;
	}

	public void setWhyareyounominatingthisVeterantobeamemberoftheNewYorkStateSenateVeteranHallofFame(
			String whyareyounominatingthisVeterantobeamemberoftheNewYorkStateSenateVeteranHallofFame) {
		WhyareyounominatingthisVeterantobeamemberoftheNewYorkStateSenateVeteranHallofFame = whyareyounominatingthisVeterantobeamemberoftheNewYorkStateSenateVeteranHallofFame;
	}

	public void setStreetAddressofVeteran(String streetAddressofVeteran) {
		StreetAddressofVeteran = streetAddressofVeteran;
	}

	public void setCityTown2(String cityTown2) {
		CityTown2 = cityTown2;
	}

	public void setZipCode2(String zipCode2) {
		ZipCode2 = zipCode2;
	}

	public void setName(String name) {
		Name = name;
	}

	public void setFilesizeKB(String filesizeKB) {
		FilesizeKB = filesizeKB;
	}
	
	public String toString() {
		return "\"" + Time + "\",\"" + IPAddress + "\",\"" + Username + "\",\"" + YourName + "\",\"" + YourAddress + "\",\"" + CityTown1 + "\",\"" + ZipCode1 + "\",\"" + YourTelephoneNumber + "\",\"" + YourEmailAddress + "\",\"" + NameofVeteran + "\",\"" + BranchofMilitaryVeteranServedin + "\",\"" + WhyareyounominatingthisVeterantobeamemberoftheNewYorkStateSenateVeteranHallofFame + "\",\"" + StreetAddressofVeteran + "\",\"" + CityTown2 + "\",\"" + ZipCode2 + "\"";
	}
	
	public static void main(String[] args) {
		System.out.println("id	first_name	middle_name	last_name	" +
    		"suffix_id	street_number	street_name	street_unit	supplemental_address_1	" +
    		"supplemental_address_2	city	state_province_id	postal_code	postal_code_suffix	" +
    		"birth_date	gender_id	phone	town_52	ward_53	election_district_49	" +
    		"congressional_distrcit_46	ny_senate_district_47	ny_assembly_district_48	" +
    		"school_district_54	county_50	email	prefix_id	current_employer	" +
    		"job_title	note	keywords	geo_code_1	geo_code_2");
	}
}

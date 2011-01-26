package model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import generated.usps.*;

@XStreamAlias("USPSResponse")
public class ValidateResponse {
	String address1;
	String address2;
	String city;
	String state;
	String zip4;
	String zip5;
	
	public ValidateResponse() {
		
	}

	public ValidateResponse(AddressValidateResponse avr, String punctuation) {
		address1 = avr.getAddress().getAddress1();
		this.setAddress2(avr.getAddress().getAddress2(), punctuation);
		city = avr.getAddress().getCity();
		state = avr.getAddress().getState();
		this.setZip4(Integer.toString(avr.getAddress().getZip4()));		
		this.setZip5(Integer.toString(avr.getAddress().getZip5()));
	}
	
	public ValidateResponse(CityStateLookupResponse ctlr) {
		city = ctlr.getZipCode().getCity();
		state = ctlr.getZipCode().getState();
		this.setZip5(Integer.toString(ctlr.getZipCode().getZip5()));
	}
	
	public ValidateResponse(ZipCodeLookupResponse zclr) {
		address1 = zclr.getAddress().getAddress2();
		city = zclr.getAddress().getCity();
		state = zclr.getAddress().getState();
		this.setZip4(Integer.toString(zclr.getAddress().getZip4()));
		this.setZip5(Integer.toString(zclr.getAddress().getZip5()));
	}
	
	public String getAddress1() {
		return address1;
	}

	public String getAddress2() {
		return address2;
	}

	public String getCity() {
		return city;
	}

	public String getState() {
		return state;
	}

	public String getZip4() {
		return zip4;
	}

	public String getZip5() {
		return zip5;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public void setAddress2(String address2, String punctuation) {
		if(punctuation == null || !punctuation.equalsIgnoreCase("true")) {
			this.address2 = address2;
		}
		else {
			this.address2 = formatAbbreviations(address2);
		}
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setZip4(String zip4) {
		while(zip4.length() < 4) {
			zip4 = "0" + zip4;
		}
		this.zip4 = zip4;
	}

	public void setZip5(String zip5) {
		while(zip5.length() < 5) {
			zip5 = "0" + zip5;
		}
		this.zip5 = zip5;
	}
	
	public String formatAbbreviations(String s) {
		Pattern p = Pattern.compile("(\\s)(ALY|ANX|ARC|AVE|BYU|BCH|BND|BLF|BLFS|BTM|BLVD|BR|BRG|BRK|BRKS|BG|BGS|BYP|CP|CYN|CPE|CSWY|CTR|CTRS|CIR|CIRS|CLF|CLFS|CLB|CMN|COR|CORS|CRSE|CT|CTS|CV|CVS|CRK|CRES|CRST|XING|XRD|CURV|DL|DM|DV|DR|DRS|EST|ESTS|EXPY|EXT|EXTS|FALL|FLS|FRY|FLD|FLDS|FLT|FLTS|FRD|FRDS|FRST|FRG|FRGS|FRK|FRKS|FT|FWY|GDN|GDNS|GTWY|GLN|GLNS|GRN|GRNS|GRV|GRVS|HBR|HBRS|HVN|HTS|HWY|HL|HLS|HOLW|INLT|IS|ISS|ISLE|JCT|JCTS|KY|KYS|KNL|KNLS|LK|LKS|LAND|LNDG|LN|LGT|LGTS|LF|LCK|LCKS|LDG|LOOP|MALL|MNR|MNRS|MDW|MDWS|MEWS|ML|MLS|MSN|MTWY|MT|MTN|MTNS|NCK|ORCH|OVAL|OPAS|PARK|PKWY|PASS|PSGE|PATH|PIKE|PNE|PNES|PL|PLN|PLNS|PLZ|PT|PTS|PRT|PRTS|PR|RADL|RAMP|RNCH|RPD|RPDS|RST|RDG|RDGS|RIV|RD|RDS|RTE|ROW|RUE|RUN|SHL|SHLS|SHR|SHRS|SKWY|SPG|SPGS|SPUR|SQ|SQS|STA|STRA|STRM|ST|STS|SMT|TER|TRWY|TRCE|TRAK|TRFY|TRL|TUNL|TPKE|UPAS|UN|UNS|VLY|VLYS|VIA|VW|VWS|VLG|VLGS|VL|VIS|WALK|WALL|WAY|WAYS|WL|WLS)($|\\s)");
		Matcher m = p.matcher(s);
		return m.replaceAll("$1$2\\.$3");
	}

	@Override
	public String toString() {
		return "ValidateResponse ["
				+ (address1 != null ? "address1=" + address1 + ", " : "")
				+ (address2 != null ? "address2=" + address2 + ", " : "")
				+ (city != null ? "city=" + city + ", " : "")
				+ (state != null ? "state=" + state + ", " : "")
				+ (zip4 != null ? "zip4=" + zip4 + ", " : "")
				+ (zip5 != null ? "zip5=" + zip5 : "") + "]";
	}
	
	
	
}

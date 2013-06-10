package gov.nysenate.sage.model.job;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Date;

public class JobProcess
{
    protected int id = -1;
    protected JobUser requestor;
    protected String sourceFileName = "";
    protected String systemFilename = "";
    protected String fileType = "";
    protected Timestamp requestTime;
    protected int recordCount = 0;
    protected boolean validationRequired = false;
    protected boolean geocodeRequired = false;
    protected boolean districtRequired = false;

	public JobProcess()
    {
        this.requestTime = new Timestamp(new Date().getTime());
	}

    public void setSourceFileName(String fileName) {
        this.sourceFileName = fileName;
    }

    public String getSourceFileName() {
		return sourceFileName;
	}

	public String getFileType() {
		return fileType;
	}

	public Timestamp getRequestTime() {
		return requestTime;
	}

	public int getId() {
		return id;
	}

	public void setFileName(String fileName) {
		this.systemFilename = fileName;
	}

    public String getFileName() {
        return this.systemFilename;
    }

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public void setRequestTime(Timestamp timeStamp) {
		this.requestTime = timeStamp;
	}

    public void setId(int id) {
		this.id = id;
	}

    public JobUser getRequestor() {
        return requestor;
    }

    public void setRequestor(JobUser requestor) {
        this.requestor = requestor;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public String getSystemFilename() {
        return systemFilename;
    }

    public void setSystemFilename(String systemFilename) {
        this.systemFilename = systemFilename;
    }

    public boolean isValidationRequired() {
        return validationRequired;
    }

    public void setValidationRequired(boolean validationRequired) {
        this.validationRequired = validationRequired;
    }

    public boolean isGeocodeRequired() {
        return geocodeRequired;
    }

    public void setGeocodeRequired(boolean geocodeRequired) {
        this.geocodeRequired = geocodeRequired;
    }

    public boolean isDistrictRequired() {
        return districtRequired;
    }

    public void setDistrictRequired(boolean districtRequired) {
        this.districtRequired = districtRequired;
    }
}

package gov.nysenate.sage.client.view.job;

import gov.nysenate.sage.model.job.JobProcess;
import gov.nysenate.sage.model.job.JobUser;

import java.sql.Timestamp;

public class JobProcessView
{
    protected int id = -1;
    protected int requestorId;
    protected String requestorEmail;
    protected String sourceFileName = "";
    protected String fileName = "";
    protected Timestamp requestTime;
    protected int recordCount = 0;
    protected boolean validationRequired = false;
    protected boolean geocodeRequired = false;
    protected boolean districtRequired = false;

    public JobProcessView(JobProcess jobProcess)
    {
        this.id = jobProcess.getId();
        if (jobProcess.getRequestor() != null) {
            this.requestorId = jobProcess.getRequestor().getId();
            this.requestorEmail = jobProcess.getRequestor().getEmail();
        }
        this.sourceFileName = jobProcess.getSourceFileName();
        this.fileName = jobProcess.getFileName();
        this.requestTime = jobProcess.getRequestTime();
        this.recordCount = jobProcess.getRecordCount();
        this.validationRequired = jobProcess.isValidationRequired();
        this.geocodeRequired = jobProcess.isGeocodeRequired();
        this.districtRequired = jobProcess.isDistrictRequired();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRequestorId() {
        return requestorId;
    }

    public void setRequestorId(int requestorId) {
        this.requestorId = requestorId;
    }

    public String getRequestorEmail() {
        return requestorEmail;
    }

    public void setRequestorEmail(String requestorEmail) {
        this.requestorEmail = requestorEmail;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Timestamp getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Timestamp requestTime) {
        this.requestTime = requestTime;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
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

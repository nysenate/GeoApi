package gov.nysenate.sage.client.view.job;

import gov.nysenate.sage.model.job.JobProcess;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
public class JobProcessView {
    private final int id;
    private int requestorId;
    private String requestorEmail;
    private final String sourceFileName;
    private final String fileName;
    private final Timestamp requestTime;
    private final int recordCount;
    private final boolean validationRequired;
    private final boolean geocodeRequired;
    private final boolean districtRequired;

    public JobProcessView(JobProcess jobProcess) {
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
}

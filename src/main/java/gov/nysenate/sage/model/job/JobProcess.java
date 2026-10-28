package gov.nysenate.sage.model.job;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Date;

@Getter
@Setter
public class JobProcess {
    private int id = -1;
    private JobUser requestor;
    private String sourceFileName = "";
    private String fileName = "";
    private String fileType = "";
    private Timestamp requestTime;
    private int recordCount = 0;
    private boolean validationRequired = false;
    private boolean geocodeRequired = false;
    private boolean districtRequired = false;

    public JobProcess() {
        this.requestTime = new Timestamp(new Date().getTime());
    }
}

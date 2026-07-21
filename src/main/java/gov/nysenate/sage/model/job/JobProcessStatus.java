package gov.nysenate.sage.model.job;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Status information for a job process.
 */
@Setter
@Getter
public class JobProcessStatus {
    public enum Condition {
        WAITING_FOR_CRON,
        RUNNING,
        COMPLETED,
        SKIPPED,
        FAILED,
        CANCELLED,
    }

    protected int processId;
    protected JobProcess jobProcess;
    protected Condition condition;
    protected int completedRecords = 0;
    protected Timestamp startTime = null;
    protected Timestamp completeTime = null;
    protected boolean completed = false;
    protected List<String> messages = new ArrayList<>();

    public JobProcessStatus() {}

    public JobProcessStatus(int processId) {
        this.processId = processId;
        this.condition = Condition.WAITING_FOR_CRON;
    }

}

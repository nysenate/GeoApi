package gov.nysenate.sage.model.job;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
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
        CANCELLED;

        public static List<Condition> getActiveConditions() {
            return Arrays.asList(Condition.WAITING_FOR_CRON, Condition.RUNNING);
        }
    }

    private int processId;
    private JobProcess jobProcess;
    private Condition condition;
    private int completedRecords = 0;
    private Timestamp startTime = null;
    private Timestamp completeTime = null;
    private boolean completed = false;
    private List<String> messages = new ArrayList<>();

    public JobProcessStatus() {}

    public JobProcessStatus(int processId) {
        this.processId = processId;
        this.condition = Condition.WAITING_FOR_CRON;
    }
}

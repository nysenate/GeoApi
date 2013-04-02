package gov.nysenate.sage.model.job;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Status information for a job process.
 */
public class JobProcessStatus
{
    public enum Condition
    {
        WAITING_FOR_CRON,
        RUNNING,
        COMPLETED,
        COMPLETED_WITH_ERRORS,
        FAILED,
        CANCELLED,
        INACTIVE;

        public static List<Condition> getActiveConditions()
        {
            return Arrays.asList(Condition.WAITING_FOR_CRON, Condition.RUNNING);
        }

        public static List<Condition> getInactiveConditions()
        {
            return Arrays.asList(Condition.COMPLETED, Condition.COMPLETED_WITH_ERRORS, Condition.FAILED,
                    Condition.CANCELLED, Condition.INACTIVE);
        }
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

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public JobProcess getJobProcess() {
        return jobProcess;
    }

    public void setJobProcess(JobProcess jobProcess) {
        this.jobProcess = jobProcess;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public int getCompletedRecords() {
        return completedRecords;
    }

    public void setCompletedRecords(int completedRecords) {
        this.completedRecords = completedRecords;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Timestamp completeTime) {
        this.completeTime = completeTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}

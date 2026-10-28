package gov.nysenate.sage.model.job;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

@Getter
public class JobRequest {
    private final JobUser requestor;
    private final List<JobProcess> processes = new LinkedList<>();

    public JobRequest(JobUser requestor) {
        this.requestor = requestor;
    }

    public void clear() {
        processes.clear();
    }

    public void addProcess(JobProcess process) {
        processes.add(process);
    }
}

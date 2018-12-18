package gov.nysenate.sage.dao.stats.exception;

import gov.nysenate.sage.model.stats.ExceptionInfo;

import java.util.List;

public interface ExceptionInfoDao {
    /**
     * Retrieves a list of all unhandled exceptions.
     * @param excludeHidden If true only non-hidden exceptions will be retrieved.
     * @return List<ExceptionInfo>
     */
    public List<ExceptionInfo> getExceptionInfoList(Boolean excludeHidden);

    /**
     * Marks an exception as hidden so that it won't appear in the interface.
     * @param id Id of the exception info.
     */
    public int hideExceptionInfo(int id);


}

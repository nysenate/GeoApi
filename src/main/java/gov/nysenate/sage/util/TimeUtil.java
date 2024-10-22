package gov.nysenate.sage.util;

import java.sql.Timestamp;
import java.util.Date;

public abstract class TimeUtil {
    public static Timestamp currentTimestamp()
    {
        return new Timestamp(new Date().getTime());
    }

    public static long getElapsedMs(Timestamp start) {
        if (start != null) {
            return currentTimestamp().getTime() - start.getTime();
        }
        return 0;
    }
}

package gov.nysenate.sage.util;

import java.sql.SQLException;

@FunctionalInterface
public interface SqlCheckedFunction<T, R> {
    R apply(T t) throws SQLException;
}

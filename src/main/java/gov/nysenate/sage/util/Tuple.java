package gov.nysenate.sage.util;

import javax.annotation.Nonnull;
import java.util.Objects;

public record Tuple<T, R>(T first, R second) {
    @Nonnull
    @Override
    public String toString() {
        return "(%s, %s)".formatted(Objects.toString(first), Objects.toString(second));
    }
}

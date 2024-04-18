package gov.nysenate.sage.util;

import java.util.Objects;

public record Tuple<T, R>(T first, R second) {
    @Override
    public String toString() {
        return "(%s, %s)".formatted(Objects.toString(first), Objects.toString(second));
    }
}

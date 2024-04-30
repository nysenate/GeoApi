package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import java.util.function.Predicate;

public record LineTest<T>(Predicate<T> test, StreetfileLineType errorType) {}

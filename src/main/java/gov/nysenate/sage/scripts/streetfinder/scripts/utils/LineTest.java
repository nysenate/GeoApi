package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import java.util.function.Predicate;

/**
 * A simple record to check if streetfile lines have problems.
 * @param test to apply.
 * @param errorType to check for.
 * @param <T> String if testing before parsing, else List<String>.
 */
public record LineTest<T>(Predicate<T> test, StreetfileLineType errorType) {}

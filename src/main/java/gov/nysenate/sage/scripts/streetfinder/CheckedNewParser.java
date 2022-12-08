package gov.nysenate.sage.scripts.streetfinder;

import gov.nysenate.sage.scripts.streetfinder.parsers.BaseParser;

import java.io.IOException;

@FunctionalInterface
public interface CheckedNewParser<P extends BaseParser<?>> {
    P apply(String filename) throws IOException;
}

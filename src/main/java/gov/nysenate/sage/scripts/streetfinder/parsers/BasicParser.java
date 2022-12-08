package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;

import java.io.IOException;

/**
 * Most classes just use StreetFinderAddress, so this is for simplification.
 */
public abstract class BasicParser extends BaseParser<StreetFileAddress> {
    public BasicParser(String filename) throws IOException {
        super(filename);
    }

    @Override
    protected StreetFileAddress getNewAddress() {
        return new StreetFileAddress();
    }
}

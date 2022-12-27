package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;

import java.io.File;

/**
 * Most classes just use StreetFinderAddress, so this is for simplification.
 */
public abstract class BasicParser extends BaseParser<StreetFileAddress> {
    public BasicParser(File file) {
        super(file);
    }

    @Override
    protected StreetFileAddress getNewAddress() {
        return new StreetFileAddress();
    }
}

package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;

import java.io.File;

/**
 * This is for simplification, as most classes just use StreetFinderAddress.
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

package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddressRange;

import java.io.File;

/**
 * This is for simplification, as most classes just use StreetFinderAddress.
 */
public abstract class BasicParser extends BaseParser<StreetFileAddressRange> {
    public BasicParser(File file) {
        super(file);
    }

    @Override
    protected StreetFileAddressRange getNewAddress() {
        return new StreetFileAddressRange();
    }
}

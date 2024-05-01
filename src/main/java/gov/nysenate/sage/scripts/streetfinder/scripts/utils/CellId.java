package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

public record CellId(String source, long id) {
    public CellId {
        source = source.intern();
    }
}

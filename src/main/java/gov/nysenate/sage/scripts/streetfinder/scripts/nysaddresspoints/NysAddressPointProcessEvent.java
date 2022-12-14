package gov.nysenate.sage.scripts.streetfinder.scripts.nysaddresspoints;

public class NysAddressPointProcessEvent {
    private int batchSize;
    private boolean saveNycToSeparateFile;

    public NysAddressPointProcessEvent(int batchSize, boolean saveNycToSeparateFile) {
        this.batchSize = batchSize;
        this.saveNycToSeparateFile = saveNycToSeparateFile;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isSaveNycToSeparateFile() {
        return saveNycToSeparateFile;
    }

    public void setSaveNycToSeparateFile(boolean saveNycToSeparateFile) {
        this.saveNycToSeparateFile = saveNycToSeparateFile;
    }
}

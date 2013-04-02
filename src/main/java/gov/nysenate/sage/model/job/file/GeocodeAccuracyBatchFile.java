package gov.nysenate.sage.model.job.file;

import gov.nysenate.sage.model.address.Address;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.ArrayList;
import java.util.List;

public class GeocodeAccuracyBatchFile extends BaseJobFile<GeocodeAccuracyBatchRecord>
{
    public List<Address> getAddresses()
    {
        List<Address> addresses = new ArrayList<>();
        for (GeocodeAccuracyBatchRecord g : this.records) {
            addresses.add(g.toAddress());
        }
        return addresses;
    }

    public static CellProcessor[] getProcessors() {
        final CellProcessor[] processors = new CellProcessor[] {
                new NotNull(),                  // street
                new NotNull(),                  // city
                new NotNull(),                  // state
                new Optional(),                 // zip5
                new Optional(),                 // zip4
                new Optional(),                 // geomethod
                new Optional(new ParseDouble()),// lat
                new Optional(new ParseDouble()),// lon
                new Optional(new ParseInt()),   // rawquality
                new Optional(),                 // quality
                new Optional(),                 // refgeomethod
                new Optional(new ParseDouble()),// reflat
                new Optional(new ParseDouble()),// reflon
                new Optional(),                 // refquality
                new Optional()                  // distance
        };
        return processors;
    }
}

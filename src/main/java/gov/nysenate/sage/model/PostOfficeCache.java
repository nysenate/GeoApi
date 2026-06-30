package gov.nysenate.sage.model;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import gov.nysenate.sage.model.address.PostOfficeBox;
import gov.nysenate.sage.model.address.Zip5;
import gov.nysenate.sage.model.result.BaseResult;
import gov.nysenate.sage.provider.DataSource;

import java.util.List;
import java.util.function.Function;

public class PostOfficeCache<Source extends DataSource, ResultType extends BaseResult<Source>> {
    private final Table<Zip5, List<Source>, PostOfficeData<ResultType>> internalCache = HashBasedTable.create();
    private final ResultType nullZipResult;
    private final Function<Multimap<String, ResultType>, PostOfficeData<ResultType>> resultsToValueMapper;

    public PostOfficeCache(ResultType nullZipResult,
                           Function<Multimap<String, ResultType>, PostOfficeData<ResultType>> resultToValueMapper) {
        this.nullZipResult = nullZipResult;
        this.resultsToValueMapper = resultToValueMapper;
    }

    public synchronized ResultType putAndGet(PostOfficeBox poBox, List<Source> sources,
                                             Multimap<String, ResultType> postalCityToResults) {
        if (poBox.getZip5() == null) {
            return nullZipResult;
        }
        PostOfficeData<ResultType> cell = resultsToValueMapper.apply(postalCityToResults);
        internalCache.put(poBox.getZip5(), sources, cell);
        return cell.getData(poBox.getPostalCity());
    }

    public ResultType get(PostOfficeBox poBox, List<Source> sources) {
        if (poBox.getZip5() == null) {
            return nullZipResult;
        }
        PostOfficeData<ResultType> cell = internalCache.get(poBox.getZip5(), sources);
        if (cell == null) {
            return null;
        }
        return cell.getData(poBox.getPostalCity());
    }

    public synchronized void clear() {
        internalCache.clear();
    }
}

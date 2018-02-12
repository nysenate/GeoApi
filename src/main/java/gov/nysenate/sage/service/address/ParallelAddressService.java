package gov.nysenate.sage.service.address;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.factory.SageThreadFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.util.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Parallel address validation for use when an AddressService implementation does not provide
 * native batch methods.
 */
@Service
public abstract class ParallelAddressService extends BaseDao {

    private static Logger logger = LogManager.getLogger(ParallelAddressService.class);
    private Config config = getConfig();
    private int THREAD_COUNT = Integer.parseInt(config.getValue("validate.threads", "3"));
    private ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT, new SageThreadFactory("address"));

    private static class ParallelValidate implements Callable<AddressResult>
    {
        public final AddressService addressService;
        public final Address address;

        public ParallelValidate(AddressService addressService, Address address)
        {
            this.addressService = addressService;
            this.address = address;
        }

        @Override
        public AddressResult call()
        {
            return addressService.validate(address);
        }
    }

    public List<AddressResult> validate(AddressService addressService, List<Address> addresses)
    {
        ArrayList<AddressResult> addressResults = new ArrayList<>();
        ArrayList<Future<AddressResult>> futureAddressResults = new ArrayList<>();

        logger.trace("Validating addresses using " + THREAD_COUNT + " threads");
        for (Address address : addresses) {
            futureAddressResults.add(executor.submit(new ParallelValidate(addressService, address)));
        }

        for (Future<AddressResult> addressResult : futureAddressResults) {
            try {
                addressResults.add(addressResult.get());
            }
            catch (InterruptedException ex) {
                logger.error(ex.getMessage());
            }
            catch (ExecutionException ex) {
                logger.error(ex.getMessage());
            }
        }
        return addressResults;
    }

    public void shutdownThread() {
        executor.shutdownNow();
    }
}

package gov.nysenate.sage.service.address;

import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.util.Config;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Parallel address validation for use when an AddressService implementation does not provide
 * native batch methods.
 */
public abstract class ParallelAddressService {

    private static Logger logger = Logger.getLogger(ParallelAddressService.class);
    private static Config config = ApplicationFactory.getConfig();
    private static int THREAD_COUNT = Integer.parseInt(config.getValue("validate.threads", "3"));

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

    public static List<AddressResult> validate(AddressService addressService, List<Address> addresses)
    {
        ArrayList<AddressResult> addressResults = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        ArrayList<Future<AddressResult>> futureAddressResults = new ArrayList<>();

        logger.debug("Validating addresses using " + THREAD_COUNT + " threads");
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
        executor.shutdown();
        return addressResults;
    }
}

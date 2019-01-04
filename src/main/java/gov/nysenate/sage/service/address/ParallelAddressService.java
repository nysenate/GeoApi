package gov.nysenate.sage.service.address;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.factory.SageThreadFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.provider.address.AddressService;
import gov.nysenate.sage.util.ExecutorUtil;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Parallel address validation for use when an AddressService implementation does not provide
 * native batch methods.
 */
@Service
public class ParallelAddressService {

    private int THREAD_COUNT;
    private static ThreadPoolTaskExecutor executor;
    private static Logger logger = LoggerFactory.getLogger(ParallelAddressService.class);
    private Environment env;

    @Autowired
    public ParallelAddressService(Environment env) {
        this.env = env;
        this.THREAD_COUNT = this.env.getValidateThreads();
        this.executor = ExecutorUtil.createExecutor("address", THREAD_COUNT);
//        this.executor = Executors.newFixedThreadPool(THREAD_COUNT, new SageThreadFactory("address"));
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
        executor.shutdown();
    }

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
}

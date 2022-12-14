package gov.nysenate.sage.scripts;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.scripts.streetfinder.scripts.nysaddresspoints.NYSAddressPointProcessor;

import java.io.IOException;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Using a test class as a simple hack to initiate this process with spring initialized.
 */
@Category(IntegrationTest.class)
public class NYSAddressPointsMain extends BaseTests {

    @Autowired
    private NYSAddressPointProcessor addressPointProcessor;

    // TODO ignore/remove test notation so this is not executed routinely.
    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void main() throws IOException {
        long startTime = System.nanoTime();
        addressPointProcessor.processNYSAddressPoints();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("-----------------------------------");
        System.out.println("Processed Address Points in: " + (duration / 1000000000) + " seconds");
        System.out.println("-----------------------------------");
    }
}

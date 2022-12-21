package gov.nysenate.sage.scripts.streetfinder.scripts.nysaddresspoints;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class NYSAddressPointProcessorTest extends BaseTests {

    @Autowired private NYSAddressPointProcessor addressPointProcessor;

    @Ignore
    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void testAddress_408E155St() {
        List<NYSAddressPoint> batch = new ArrayList<>();
        String[] data = new String[]{"2832961", "", "408", "", "155", "", "Bronx", "NY", "10455", "1", "New York",
                "Bronx", "", "East 155 Street", "Active", "408 E 155 St", "Bronx", "US", "", "", "", "",
                "40.819910778132100", "-73.915491543349100", "29", "79", "15"};
        batch.add(new NYSAddressPoint(data));

        AddressPointValidationResult actual = addressPointProcessor.processBatch(batch).get(0);
        // Validation fails with city = New York but city = "" or city = "Bronx" works.
        assertTrue(actual.validationResult().isValidated());
        assertEquals("E 155 ST", actual.fullStreetName());
    }

    @Ignore
    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void testAddress_386E154St() {
        List<NYSAddressPoint> batch = new ArrayList<>();
        String[] data = new String[]{"2832666", "", "386", "", "154", "", "Bronx", "NY", "10455", "1", "New York",
                "Bronx", "", "East 154 Street", "Active", "386 E 154 St", "Bronx", "US", "", "", "", "",
                "40.819577504471800", "-73.916860058163100", "29", "79", "15"};
        batch.add(new NYSAddressPoint(data));

        AddressPointValidationResult actual = addressPointProcessor.processBatch(batch).get(0);
        assertTrue(actual.validationResult().isValidated());
        assertEquals("E 154 ST", actual.fullStreetName());
    }

    @Ignore
    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void testAddress_RainbowSt() {
        List<NYSAddressPoint> batch = new ArrayList<>();
        // TODO Only validates if city = PlaceName.
        String[] data = new String[]{"2164958", "", "42017", "", "Rainbow", "", "Wellesley Island", "NY", "13650", "1",
                "Orleans", "Thousand Island Park", "", "Rainbow Street West", "Active", "42017 Rainbow St W", "Jefferson",
                "US", "Orleans", "Thousand Island Park", "2", "", "44.288728084016", "-76.0302426886762", "49", "116", "24"};
        batch.add(new NYSAddressPoint(data));

        AddressPointValidationResult actual = addressPointProcessor.processBatch(batch).get(0);
        assertTrue(actual.validationResult().isValidated());
        assertEquals("RAINBOW ST W", actual.fullStreetName());
    }

    @Ignore
    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void testAddress_JeffersonvilleNorthBranch() {
        List<NYSAddressPoint> batch = new ArrayList<>();
        String[] data = new String[]{"3507261", "", "591", "", "Jeffersonville North Branch", "", "North Branch", "NY", "12766", "1",
                "Callicoon", "", "", "Jeffersonville North Branch Road", "Active", "591 Jeffersonville North Branch Rd", "Sullivan",
                "US", "Callicoon", "", "2", "", "41.8020063789871", "-74.9675022538552", "51", "100", "19"};
        batch.add(new NYSAddressPoint(data));

        AddressPointValidationResult actual = addressPointProcessor.processBatch(batch).get(0);
        assertTrue(actual.validationResult().isValidated());
        assertEquals("JEFFERSONVILLE N BRANCH RD", actual.fullStreetName());
    }
}

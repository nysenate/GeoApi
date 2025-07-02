package gov.nysenate.sage;

import gov.nysenate.sage.config.ConsoleApplicationConfig;
import gov.nysenate.sage.config.DatabaseConfig;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class, ConsoleApplicationConfig.class})
@ActiveProfiles("test")
@Transactional(DatabaseConfig.geoApiTxManager)
public abstract class BaseTests {}
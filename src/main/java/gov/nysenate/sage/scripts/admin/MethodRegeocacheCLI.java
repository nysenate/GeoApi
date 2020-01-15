package gov.nysenate.sage.scripts.admin;

import gov.nysenate.sage.scripts.BaseScript;
import gov.nysenate.sage.service.data.DataGenService;
import gov.nysenate.sage.service.data.RegeocacheService;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MethodRegeocacheCLI extends BaseScript {

    private static final Logger logger = LoggerFactory.getLogger(MethodRegeocacheCLI.class);

    @Autowired
    RegeocacheService regeocacheService;

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext ctx = init();
        MethodRegeocacheCLI generateMetaDataCLI = ctx.getBean(MethodRegeocacheCLI.class);
        CommandLine cmd = getCommandLine(generateMetaDataCLI.getOptions(), args);
        generateMetaDataCLI.execute(cmd);
        shutdown(ctx);
        System.exit(0);
    }

    @Override
    protected Options getOptions() {
        Options options = super.getOptions();
        return options;
    }

    @Override
    protected void execute(CommandLine opts) throws Exception {


        if (!opts.getArgList().isEmpty()) {

            String[] options = opts.getArgs();

            int offset = Integer.parseInt( options[0] );
            String method = options[1];

            if (method.equals("nysgeodb")) {
                method = "NYS Geo DB";
            }

            logger.info("offset is " + offset);
            logger.info("Method is " + method);

            regeocacheService.regeocacheSpecificMethodWithNysGeoWebService( offset, method );

        }
        else {
            logger.warn("You must specify arguments. " +
                    "Acceptable arguments are: all, assembly, senate, congress, a, c, d");
        }

        logger.info("Execution Complete...Exiting");
    }

}
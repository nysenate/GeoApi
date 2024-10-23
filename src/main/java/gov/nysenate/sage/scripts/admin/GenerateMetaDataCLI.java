package gov.nysenate.sage.scripts.admin;

import gov.nysenate.sage.scripts.BaseScript;
import gov.nysenate.sage.service.data.DataGenService;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class GenerateMetaDataCLI extends BaseScript {
    private static final Logger logger = LoggerFactory.getLogger(GenerateMetaDataCLI.class);
    private final DataGenService dataGenService;

    @Autowired
    public GenerateMetaDataCLI(DataGenService dataGenService) {
        this.dataGenService = dataGenService;
    }

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext ctx = init();
        GenerateMetaDataCLI generateMetaDataCLI = ctx.getBean(GenerateMetaDataCLI.class);
        CommandLine cmd = getCommandLine(generateMetaDataCLI.getOptions(), args);
        generateMetaDataCLI.execute(cmd);
        shutdown(ctx);
        System.exit(0);
    }

    @Override
    protected void execute(CommandLine opts) throws Exception {

        if (!opts.getArgList().isEmpty()) {

            for (String opt : opts.getArgs()) {
                logger.info("Beginning Meta Data Generation for arg: {}", opt);
                dataGenService.generateMetaData(opt);
            }

        }
        else {
            logger.warn("You must specify arguments. " +
                    "Acceptable arguments are: all, assembly, senate, congress, a, s, c");
        }

        logger.info("Execution Complete...Exiting");
    }

}
package gov.nysenate.sage.scripts.admin;

import gov.nysenate.sage.scripts.BaseScript;
import gov.nysenate.sage.service.data.RegeocacheService;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

@Component
public class MassRegeocacheCLI extends BaseScript {

    private static final Logger logger = LoggerFactory.getLogger(MassRegeocacheCLI.class);

    @Autowired
    RegeocacheService regeocacheService;

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext ctx = init();
        MassRegeocacheCLI massRegeocacheCLI = ctx.getBean(MassRegeocacheCLI.class);
        CommandLine cmd = getCommandLine(massRegeocacheCLI.getOptions(), args);
        massRegeocacheCLI.execute(cmd);
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

            ArrayList<String> typeList = new ArrayList<>();
            String[] options = opts.getArgs();

            Integer offset = Integer.parseInt( options[0] );
            Integer limit = Integer.parseInt( options[1] ); //Not applicable if type is all
            boolean useFallback = Boolean.parseBoolean( options[2] );


            String[] optionsWithoutLimitAndOffset = Arrays.copyOfRange(options, 3, options.length);


            for (int i = 0; i < optionsWithoutLimitAndOffset.length; i = i + 2) {
                String type = optionsWithoutLimitAndOffset[i].toLowerCase();
                String subtype = optionsWithoutLimitAndOffset[i+1].toLowerCase();
                ArrayList<String> fixedDataForType1 = correctSubtypes(limit, type, subtype);
                limit = Integer.parseInt( fixedDataForType1.get(0) );
                subtype = fixedDataForType1.get(1);
                typeList.add(type);
                typeList.add(subtype);
            }

            if (typeList.size() == 2 && typeList.get(0).equals("provider")) {
                logger.warn("You must specify another type argument and its subtype." +
                        " all, method, town, quality, zipcode, provider");
                throw new Exception("You must specify another type argument and its subtype." +
                        " all, method, town, quality, zipcode, provider");
            }

            Object response = regeocacheService.massRegeoache(offset, limit, useFallback ,typeList);
            logger.info(response.toString());
        }
        else {
            logger.warn("You must specify arguments. offset, limit, useFallback, type, subtype, " +
                    "and then additional types and sub types");
        }

        logger.info("Execution Complete...Exiting");
    }

    private String fixNYSGeoDBMethod(String method) {
        if (method.equals("nysgeodb")) {
            method = "NYS Geo DB";
        }
        return method;
    }

    private ArrayList<String> correctSubtypes(Integer limit, String type, String subtype) throws Exception {
        switch(type) {
            case "all":
                limit = 0;
                subtype = fixNYSGeoDBMethod(subtype);
                break;
            case "method":
                subtype = fixNYSGeoDBMethod(subtype);
                break;
            case "town":
                subtype = subtype.replaceAll(",", " ").replaceAll("_", " ");
                subtype = subtype.toUpperCase();
                break;
            case "provider":
                subtype = fixNYSGeoDBMethod(subtype);
                break;
            case "quality":
                subtype = subtype.toUpperCase();
            case "zipcode":
                //No changes need to be made, but the case must be accounted for to not throw an exception
                break;
            default:
                logger.warn("You must specify a type argument. all, method, town, quality, zipcode, provider");
                throw new Exception("You must specify a type argument. all, method, town, quality, zipcode, provider");
        }

        ArrayList<String> fixedData = new ArrayList<>();
        fixedData.add(limit.toString());
        fixedData.add(subtype);
        return fixedData;
    }

}
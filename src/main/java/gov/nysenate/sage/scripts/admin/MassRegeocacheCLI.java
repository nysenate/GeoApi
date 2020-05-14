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

@Component
public class MassRegeocacheCLI extends BaseScript {

    private static final Logger logger = LoggerFactory.getLogger(MassRegeocacheCLI.class);

    @Autowired
    RegeocacheService regeocacheService;

    public static void main(String[] args) throws Exception {
//        String[] args = new String[9];
//        args[0] = "0";
//        args[1] = "2005";
//        args[2] = "true";
//        args[3] = "quality";
//        args[4] = "house";
//        args[5] = "provider";
//        args[6] = "nysgeo";
//        args[7] = "town";
//        args[8] = "clifton_park";
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

        //offset, limit, type, useFallback, subtype, type2, subtype2, type3, subtype3
        //types - all, method, town, quality, zipcode, provider

        //subtypes for all is the provider (All sets the limit to 0)
        //subtypes for method is the provider name to regeocache on
        //subtypes for town is the town name & provider is optional
        //subtypes for qualtiy is the qualty rating found in GeocodeQuality.java
        //subtypes for zipcode is the zipcode itself & provider is optional
        //subtypes for provider is the provider you would like to contact

        if (!opts.getArgList().isEmpty()) {

            ArrayList<String> typeList = new ArrayList<>();
            String[] options = opts.getArgs();

            Integer offset = Integer.parseInt( options[0] );
            Integer limit = Integer.parseInt( options[1] ); //Not applicable if type is all
            boolean useFallback = Boolean.parseBoolean( options[2] );


            String[] optionsWithoutLimitAndOffset = Arrays.copyOfRange(options, 3, options.length);


            for (int i = 0; i < optionsWithoutLimitAndOffset.length; i = i + 2) {
                String type = optionsWithoutLimitAndOffset[i];
                String subtype = optionsWithoutLimitAndOffset[i+1];
                ArrayList<String> fixedDataForType1 = correctSubtypes(limit, type, subtype);
                limit = Integer.parseInt( fixedDataForType1.get(0) );
                subtype = fixedDataForType1.get(1);
                typeList.add(type);
                typeList.add(subtype);
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
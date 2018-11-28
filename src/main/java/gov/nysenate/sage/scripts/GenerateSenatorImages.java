package gov.nysenate.sage.scripts;

import gov.nysenate.sage.dao.model.SqlSenateDao;
import gov.nysenate.sage.util.ImageUtil;
import gov.nysenate.services.model.Senator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static gov.nysenate.sage.scripts.BaseScript.getCommandLine;

public class GenerateSenatorImages
{
    @Autowired
    SqlSenateDao sqlSenateDao;

    private static Logger logger = LoggerFactory.getLogger(GenerateSenatorImages.class);

    public void execute(CommandLine opts) throws Exception
    {
        String[] args = opts.getArgs();

        if (args.length < 3) {
            printUsage();
            return;
        }

        this.generateImages(args);
    }

    public void generateImages(String[] args) throws IOException {
        String path = args[1];
        Integer height = Integer.parseInt(args[2]);
        Collection<Senator> senators = sqlSenateDao.getSenators();
        for (Senator senator : senators) {
            String filePath =  path + senator.getShortName() + ".png";
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            String baseImageDir = "http://www.nysenate.gov/files/profile-pictures/";
            String url = baseImageDir + senator.getImageUrl().replace(baseImageDir, "").replace(" ", "%20");
            ImageUtil.saveResizedImage(url, "png", file, height);
        }
    }

    private static void printUsage()
    {
        logger.error("Usage: GenerateSenatorImages DIR DESIRED_HEIGHT");
    }


    public static void main(String[] args) throws Exception {
        logger.info("running");
        CommandLine cmd = getCommandLine(new Options(), args);
        new DoBatchGeocache().execute(cmd);
    }

}

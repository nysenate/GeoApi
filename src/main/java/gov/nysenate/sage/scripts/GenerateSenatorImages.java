package gov.nysenate.sage.scripts;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.model.SenateDao;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.util.ImageUtil;
import gov.nysenate.services.model.Senator;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class GenerateSenatorImages
{
    public static void main(String[] args) throws IOException
    {
        if (args.length < 3) {
            printUsage();
            return;
        }

        String path = args[1];
        Integer height = Integer.parseInt(args[2]);

        SenateDao senateDao = new SenateDao(new Environment());
        Collection<Senator> senators = senateDao.getSenators();
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
        System.err.println("Usage: GenerateSenatorImages DIR DESIRED_HEIGHT");
    }

}

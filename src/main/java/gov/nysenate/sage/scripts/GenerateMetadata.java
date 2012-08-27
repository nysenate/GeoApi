package gov.nysenate.sage.scripts;

import gov.nysenate.sage.util.ApiController;
import gov.nysenate.sage.util.AssemblyScraper;
import gov.nysenate.sage.util.CongressScraper;
import gov.nysenate.sage.util.NYSenateServices;
import gov.nysenate.sage.util.Resource;

public class GenerateMetadata {

    public static void main(String[] args) throws Exception {
        ApiController controller = new ApiController();
        Resource APP_CONFIG = new Resource();
        String DEFAULT_WRITE_DIRECTORY = APP_CONFIG.fetch("json.directory");
        String DEFAULT_RAW_WRITE_DIRECTORY = APP_CONFIG.fetch("json.raw_directory");
        String DEFAULT_ZOOM_PATH = APP_CONFIG.fetch("json.zoom");

        System.out.println("indexing assembly... ");
        new AssemblyScraper().index();
        System.out.println("indexing congress... ");
        new CongressScraper().index();
        System.out.print("indexing senate... ");
        new NYSenateServices().index();
        System.out.println();

        System.out.println("Writing JSON");
        controller.writeJson(DEFAULT_WRITE_DIRECTORY, DEFAULT_ZOOM_PATH, true);
        System.out.println("Writing Raw JSON");
        controller.writeJson(DEFAULT_RAW_WRITE_DIRECTORY, DEFAULT_ZOOM_PATH, false);
    }

}

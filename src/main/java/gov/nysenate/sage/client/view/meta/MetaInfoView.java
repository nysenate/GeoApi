package gov.nysenate.sage.client.view.meta;

import org.apache.maven.model.Developer;
import org.apache.maven.model.Model;

import java.util.List;

public class MetaInfoView
{
    protected List<Developer> developers;
    protected String version;

    public MetaInfoView(Model pomModel)
    {
        if (pomModel != null) {
            this.version = pomModel.getVersion();
            this.developers = pomModel.getDevelopers();
        }
    }

    public List<Developer> getDevelopers() {
        return developers;
    }

    public String getVersion() {
        return version;
    }
}

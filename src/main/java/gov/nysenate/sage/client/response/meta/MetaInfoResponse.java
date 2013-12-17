package gov.nysenate.sage.client.response.meta;

import gov.nysenate.sage.client.view.meta.MetaInfoView;
import org.apache.maven.model.Model;

public class MetaInfoResponse
{
    protected MetaInfoView info;

    public MetaInfoResponse(Model pomModel)
    {
        this.info = new MetaInfoView(pomModel);
    }

    public MetaInfoView getInfo() {
        return info;
    }
}

package gov.nysenate.sage.model.district;

/**
 * Simple model for a Congressional member.
 */
public class Congressional extends DistrictMember
{
    public Congressional() {}

    public Congressional(int district, String memberName, String memberUrl)
    {
        super(district, memberName, memberUrl);
    }
}

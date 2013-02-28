package gov.nysenate.sage.model.district;

/**
 * Simple model for an Assembly member.
 */
public class Assembly extends DistrictMember
{
    public Assembly() {}

    public Assembly(int district, String memberName, String memberUrl)
    {
        super(district, memberName, memberUrl);
    }
}

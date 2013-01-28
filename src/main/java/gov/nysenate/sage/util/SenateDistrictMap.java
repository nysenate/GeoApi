package gov.nysenate.sage.util;

import gov.nysenate.sage.service.DistrictService;


/**
 * @author Ken Zalewski
 */
public class SenateDistrictMap extends DistrictMap
{
  /**
   * Constructor.
   *
   * @param district The district number
  */
  public SenateDistrictMap(int district)
  {
    super(DistrictService.TYPE.SENATE, district);
  } // SenateDistrictMap()

} // SenateDistrictMap

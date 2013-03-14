package gov.nysenate.sage.service.district;

import gov.nysenate.sage.dao.model.AssemblyDao;
import gov.nysenate.sage.dao.model.CongressionalDao;
import gov.nysenate.sage.dao.model.SenateDao;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.result.DistrictResult;

import static gov.nysenate.sage.model.district.DistrictType.ASSEMBLY;
import static gov.nysenate.sage.model.district.DistrictType.CONGRESSIONAL;
import static gov.nysenate.sage.model.district.DistrictType.SENATE;

/**
 * Typically when the district service providers return a DistrictInfo, only the district codes
 * and maps are relevant. This class provides methods to populate the remaining data which includes
 * the district members and the senator information. Since this information is not always required, this
 * functionality should be invoked through a controller as opposed to the provider implementations.
 */
public abstract class DistrictServiceMetadata
{
    /**
     * Sets the senator, congressional, and assembly member data to the district result.
     * @param districtResult
     * @return true if all were assigned
     *         false otherwise
     */
    public static void assignDistrictMetadata(DistrictResult districtResult)
    {
        /** Proceed on either a success or partial result */
        if (districtResult.isSuccess() || districtResult.isPartialSuccess()) {
            DistrictInfo districtInfo = districtResult.getDistrictInfo();

            /** Set the Senate, Congressional, and Assembly data using the respective daos */
            if (districtInfo.hasDistrictCode(SENATE)) {
                int senateCode = Integer.parseInt(districtInfo.getDistCode(SENATE));
                districtInfo.setSenator(new SenateDao().getSenatorByDistrict(senateCode));
            }
            if (districtInfo.hasDistrictCode(CONGRESSIONAL)) {
                int congressionalCode = Integer.parseInt(districtInfo.getDistCode(CONGRESSIONAL));
                districtInfo.setDistrictMember(CONGRESSIONAL, new CongressionalDao().getCongressionalByDistrict(congressionalCode));
            }
            if (districtInfo.hasDistrictCode(ASSEMBLY)) {
                int assemblyCode = Integer.parseInt(districtInfo.getDistCode(ASSEMBLY));
                districtInfo.setDistrictMember(ASSEMBLY, new AssemblyDao().getAssemblyByDistrict(assemblyCode));
            }

            districtResult.setDistrictInfo(districtInfo);
        }
    }
}

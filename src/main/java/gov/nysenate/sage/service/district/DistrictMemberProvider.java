package gov.nysenate.sage.service.district;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.model.assembly.SqlAssemblyDao;
import gov.nysenate.sage.dao.model.congressional.SqlCongressionalDao;
import gov.nysenate.sage.dao.model.senate.SqlSenateDao;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictOverlap;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.MapResult;
import gov.nysenate.services.model.Senator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * Typically when the district service providers return a DistrictInfo, only the district codes
 * and maps are provided. This class provides methods to populate the remaining data which includes
 * the district members and the senator information. Since this information is not always required, this
 * functionality should be invoked through a controller as opposed to the provider implementations.
 */
@Component
public class DistrictMemberProvider implements SageDistrictMemberProvider
{
    private Environment env;
    private SqlSenateDao sqlSenateDao;
    private SqlAssemblyDao sqlAssemblyDao;
    private SqlCongressionalDao sqlCongressionalDao;

    public DistrictMemberProvider(Environment env, SqlSenateDao sqlSenateDao, SqlAssemblyDao sqlAssemblyDao,
                                  SqlCongressionalDao sqlCongressionalDao) {
        this.env = env;
        this.sqlSenateDao = sqlSenateDao;
        this.sqlAssemblyDao = sqlAssemblyDao;
        this.sqlCongressionalDao = sqlCongressionalDao;
    }

    /**
     * Sets the senator, congressional, and assembly member data to the district result.
     * @param districtResult
     */
    public void assignDistrictMembers(DistrictResult districtResult)
    {

        /** Proceed on either a success or partial result */
        if (districtResult.isSuccess()) {
            DistrictInfo districtInfo = districtResult.getDistrictInfo();
            if (districtInfo != null) {
                /** Set the Senate, Congressional, and Assembly data using the respective daos */
                if (districtInfo.hasDistrictCode(SENATE)) {
                    int senateCode = Integer.parseInt(districtInfo.getDistCode(SENATE));
                    districtInfo.setSenator(sqlSenateDao.getSenatorByDistrict(senateCode));
                }
                if (districtInfo.hasDistrictCode(CONGRESSIONAL)) {
                    int congressionalCode = Integer.parseInt(districtInfo.getDistCode(CONGRESSIONAL));
                    districtInfo.setDistrictMember(CONGRESSIONAL, sqlCongressionalDao.getCongressionalByDistrict(congressionalCode));
                }
                if (districtInfo.hasDistrictCode(ASSEMBLY)) {
                    int assemblyCode = Integer.parseInt(districtInfo.getDistCode(ASSEMBLY));
                    districtInfo.setDistrictMember(ASSEMBLY, sqlAssemblyDao.getAssemblyByDistrict(assemblyCode));
                }

                /** Fill in neighbor district senator info */
                for (DistrictMap districtMap : districtInfo.getNeighborMaps(SENATE)) {
                    districtMap.setSenator(sqlSenateDao.getSenatorByDistrict(Integer.parseInt(districtMap.getDistrictCode())));
                }

                /** Fill in senator members if overlap exists */
                DistrictOverlap senateOverlap = districtInfo.getDistrictOverlap(SENATE);
                if (senateOverlap != null) {
                    Map<String, Senator> senatorMap = new HashMap<>();
                    for (String district : senateOverlap.getTargetDistricts()) {
                        Senator senator = sqlSenateDao.getSenatorByDistrict(Integer.parseInt(district));
                        senatorMap.put(district, senator);
                    }
                    senateOverlap.setTargetSenators(senatorMap);
                }
            }
        }
    }

    /**
     * Sets the senator, congressional, and assembly member data to the map result.
     * @param mapResult
     */
    public void assignDistrictMembers(MapResult mapResult)
    {
        if (mapResult != null && mapResult.isSuccess()) {
            for (DistrictMap map : mapResult.getDistrictMaps()) {
                if (map.getDistrictType().equals(SENATE)) {
                    int senateCode = Integer.parseInt(map.getDistrictCode());
                    map.setSenator(sqlSenateDao.getSenatorByDistrict(senateCode));
                }
                else if (map.getDistrictType().equals(CONGRESSIONAL)) {
                    int congressionalCode = Integer.parseInt(map.getDistrictCode());
                    map.setMember(sqlCongressionalDao.getCongressionalByDistrict(congressionalCode));
                }
                else if (map.getDistrictType().equals(ASSEMBLY)) {
                    int assemblyCode = Integer.parseInt(map.getDistrictCode());
                    map.setMember(sqlAssemblyDao.getAssemblyByDistrict(assemblyCode));
                }
            }
        }
    }
}

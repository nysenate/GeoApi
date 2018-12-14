package gov.nysenate.sage.dao.logger.district;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.dao.base.SqlTable;

public enum DistrictRequestQuery implements BasicSqlQuery {
    INSERT_REQUEST("INSERT INTO ${schema}. " + SqlTable.DISTRICT_REQUEST + "(apiRequestId, jobProcessId, addressId, provider, geoProvider, showMembers, showMaps, uspsValidate, skipGeocode, districtStrategy, requestTime) \n" +
            "VALUES (:apiRequestId, :jobProcessId, :addressId, :provider, :geoProvider, :showMembers, :showMaps, :uspsValidate, :skipGeocode, :districtStrategy, :requestTime) \n" +
            "RETURNING id")
    ;

    private String sql;

    DistrictRequestQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return this.sql;
    }
}

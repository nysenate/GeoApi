package gov.nysenate.sage.dao.model.member;

import gov.nysenate.sage.dao.base.BasicSqlQuery;
import gov.nysenate.sage.model.district.DistrictType;

import java.util.Map;

public enum MemberQuery implements BasicSqlQuery {
    CREATE_TABLE("""
            CREATE TABLE IF NOT EXISTS ${schema}.${memberTable} (
            district INT PRIMARY KEY,
            data JSONB NOT NULL
        )"""),

    TRUNCATE_MEMBERS("TRUNCATE ${schema}.${memberTable}"),

    UPSERT_MEMBER("""
            INSERT INTO ${schema}.${memberTable} (district, data) VALUES (:district, :data::JSONB)
            ON CONFLICT (district)
            DO UPDATE SET data = EXCLUDED.data::JSONB"""),

    GET_ALL_MEMBERS("SELECT * FROM ${schema}.${memberTable}");

    private final String sql;

    MemberQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return sql;
    }

    public String getSql(DistrictType type) {
        return getSql("members", Map.of("memberTable", type.name().toLowerCase()));
    }
}

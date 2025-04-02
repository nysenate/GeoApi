package gov.nysenate.sage.dao.model.member;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.SqlTable;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.model.district.DistrictType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class MemberDao extends BaseDao {
    public List<DistrictMember> getMembers(DistrictType districtType) {
        SqlTable currTable = getTable(districtType);
        if (currTable == null) {
            return List.of();
        }
        String sql = MemberQuery.GET_ALL_MEMBERS.getSql(getPublicSchema(), Map.of("memberTable", currTable.name()));
        return namedJdbcTemplate.query(sql, new MemberHandler(districtType));
    }

    public DistrictMember getMemberByDistrict(DistrictType districtType, int district) {
        SqlTable currTable = getTable(districtType);
        if (currTable == null) {
            return null;
        }
        var params = new MapSqlParameterSource("district", district);
        String sql = MemberQuery.GET_MEMBER_BY_DISTRICT.getSql(getPublicSchema(), Map.of("memberTable", currTable.name()));
        List<DistrictMember> assemblyList = namedJdbcTemplate.query(sql, params, new MemberHandler(districtType));
        if (assemblyList.isEmpty()) {
            return null;
        }
        return assemblyList.get(0);
    }

    public void insertDistrictMember(DistrictMember member) {
        SqlTable currTable = getTable(member.districtType());
        if (currTable == null) {
            throw new RuntimeException(member.districtType() + " does not have district members.");
        }
        var params = new MapSqlParameterSource("district", member.district())
                .addValue("memberName", member.memberName())
                .addValue("memberUrl", member.memberUrl());
        String sql = MemberQuery.INSERT_MEMBER.getSql(getPublicSchema(), Map.of("memberTable", currTable.name()));
        namedJdbcTemplate.update(sql, params);
    }

    public void deleteDistrictMember(DistrictType districtType, int district) {
        SqlTable currTable = getTable(districtType);
        if (currTable == null) {
            return;
        }
        var params = new MapSqlParameterSource("district", district);
        String sql = MemberQuery.INSERT_MEMBER.getSql(getPublicSchema(), Map.of("memberTable", currTable.name()));
        namedJdbcTemplate.update(sql, params);
    }

    private record MemberHandler(DistrictType districtType) implements RowMapper<DistrictMember> {
        @Override
            public DistrictMember mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new DistrictMember(districtType, rs.getInt("district"),
                        rs.getString("membername"), rs.getString("memberurl"));
            }
        }

    private static SqlTable getTable(DistrictType districtType) {
        return switch (districtType) {
            case ASSEMBLY -> SqlTable.PUBLIC_ASSEMBLY;
            case SENATE ->  SqlTable.PUBLIC_SENATE;
            case CONGRESSIONAL ->   SqlTable.PUBLIC_CONGRESSIONAL;
            default -> null;
        };
    }
}

package gov.nysenate.sage.dao.model.member;

import gov.nysenate.sage.dao.base.BaseDao;
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
        String currTable = getTable(districtType);
        if (currTable == null) {
            return List.of();
        }
        String sql = MemberQuery.GET_ALL_MEMBERS.getSql(getPublicSchema(), Map.of("memberTable", currTable));
        return namedJdbcTemplate.query(sql, new MemberHandler(districtType));
    }

    public void insertOrReplaceDistrictMember(DistrictMember member) {
        String currTable = getTable(member.districtType());
        if (currTable == null) {
            throw new RuntimeException(member.districtType() + " does not have district members.");
        }
        var params = new MapSqlParameterSource("district", member.district())
                .addValue("memberName", member.memberName())
                .addValue("memberUrl", member.memberUrl());
        String sql = MemberQuery.DELETE_MEMBER.getSql(getPublicSchema(), Map.of("memberTable", currTable));
        namedJdbcTemplate.update(sql, params);
        sql = MemberQuery.INSERT_MEMBER.getSql(getPublicSchema(), Map.of("memberTable", currTable));
        namedJdbcTemplate.update(sql, params);
    }

    private record MemberHandler(DistrictType districtType) implements RowMapper<DistrictMember> {
        @Override
        public DistrictMember mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new DistrictMember(districtType, rs.getInt("district"),
                    rs.getString("member_name"), rs.getString("member_url"));
        }
    }

    private static String getTable(DistrictType districtType) {
        return switch (districtType) {
            case ASSEMBLY, CONGRESSIONAL -> districtType.name().toLowerCase();
            default -> null;
        };
    }
}

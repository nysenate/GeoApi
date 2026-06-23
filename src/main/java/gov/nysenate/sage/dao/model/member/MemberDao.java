package gov.nysenate.sage.dao.model.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.util.FormatUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Repository
public class MemberDao extends BaseDao {
    private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    public void refreshMemberData(DistrictType type, Map<Long, DistrictMember> newMemberMap) {
        synchronized (mapper) {
            jdbcTemplate.execute(MemberQuery.CREATE_TABLE.getSql(type));
            jdbcTemplate.execute(MemberQuery.TRUNCATE_MEMBERS.getSql(type));
            for (var entry : newMemberMap.entrySet()) {
                var params = new MapSqlParameterSource("district", entry.getKey())
                        .addValue("data", FormatUtil.toJsonString(entry.getValue()));
                String sql = MemberQuery.UPSERT_MEMBER.getSql(type);
                namedJdbcTemplate.update(sql, params);
            }
        }
    }

    public Map<Long, DistrictMember> getMembers(DistrictType type) {
        String sql = MemberQuery.GET_ALL_MEMBERS.getSql(type);
        var handler = new MemberHandler();
        synchronized (mapper) {
            try {
                namedJdbcTemplate.query(sql, handler);
            }
            // Thrown if the table does not exist.
            catch (BadSqlGrammarException e) {
                return null;
            }
        }
        return handler.memberMap;
    }

    private static class MemberHandler implements RowCallbackHandler {
        private final Map<Long, DistrictMember> memberMap = new HashMap<>();

        @Override
        public void processRow(@NonNull ResultSet rs) throws SQLException {
            try {
                memberMap.put(rs.getLong("district"), mapper.readValue(rs.getString("data"), DistrictMember.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

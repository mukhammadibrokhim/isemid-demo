package uz.uzinfocom.app.platform.audit.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code AuditEvent.changes} is persisted via {@code @JdbcTypeCode(SqlTypes.JSON)},
 * which Hibernate backs with a Jackson {@code ObjectMapper} that auto-registers
 * whatever modules (e.g. {@code JavaTimeModule}) are on the classpath — the same
 * mechanism, not a hand-rolled one. This test exercises that mapper directly
 * against a realistic multi-field, mixed-type diff (the kind {@link AuditFieldDiff#compute}
 * actually produces from {@code auditFields()} snapshots: enum, String, Long,
 * LocalDateTime, null) to confirm every changed field round-trips, not just the
 * first one.
 */
class AuditEventChangesSerializationTest {

    private enum SampleStatus {SENT, APPROVED}

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void serializesAndDeserializesEveryChangedFieldWithMixedTypes() throws Exception {
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("status", SampleStatus.SENT);
        before.put("receiverOrganizationId", 5L);
        before.put("finalIcd10Code", null);
        before.put("sampleTakenDateTime", LocalDateTime.of(2026, 1, 1, 9, 0));
        before.put("unchangedField", "same");

        Map<String, Object> after = new LinkedHashMap<>();
        after.put("status", SampleStatus.APPROVED);
        after.put("receiverOrganizationId", 8L);
        after.put("finalIcd10Code", "A00");
        after.put("sampleTakenDateTime", LocalDateTime.of(2026, 1, 2, 10, 30));
        after.put("unchangedField", "same");

        Map<String, Object> changes = AuditFieldDiff.compute(before, after);
        assertThat(changes).containsOnlyKeys("status", "receiverOrganizationId", "finalIcd10Code", "sampleTakenDateTime");

        String json = objectMapper.writeValueAsString(changes);
        @SuppressWarnings("unchecked")
        Map<String, Object> roundTripped = objectMapper.readValue(json, Map.class);

        assertThat(roundTripped).containsOnlyKeys("status", "receiverOrganizationId", "finalIcd10Code", "sampleTakenDateTime");
        assertThat(roundTripped.get("status")).isEqualTo(java.util.List.of("SENT", "APPROVED"));
        assertThat(roundTripped.get("receiverOrganizationId")).isEqualTo(java.util.List.of(5, 8));
        assertThat(roundTripped.get("finalIcd10Code")).isEqualTo(java.util.Arrays.asList(null, "A00"));
    }
}

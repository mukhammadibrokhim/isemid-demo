package uz.uzinfocom.app.platform.audit.domain;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuditFieldDiffTest {

    @Test
    void returnsEmptyMapWhenNothingChanged() {
        Map<String, Object> before = snapshot("status", "SENT", "receiverOrganizationId", 1L);
        Map<String, Object> after = snapshot("status", "SENT", "receiverOrganizationId", 1L);

        assertThat(AuditFieldDiff.compute(before, after)).isEmpty();
    }

    @Test
    void reportsOnlyChangedFieldsAsOldNewPairs() {
        Map<String, Object> before = snapshot("status", "SENT", "receiverOrganizationId", 1L, "finalIcd10Code", null);
        Map<String, Object> after = snapshot("status", "APPROVED", "receiverOrganizationId", 1L, "finalIcd10Code", "A00");

        Map<String, Object> changes = AuditFieldDiff.compute(before, after);

        assertThat(changes).containsOnlyKeys("status", "finalIcd10Code");
        assertThat((Object[]) changes.get("status")).containsExactly("SENT", "APPROVED");
        assertThat((Object[]) changes.get("finalIcd10Code")).containsExactly(null, "A00");
    }

    @Test
    void treatsFieldMissingFromBeforeAsPreviouslyNull() {
        Map<String, Object> before = snapshot();
        Map<String, Object> after = snapshot("patientId", 42L);

        Map<String, Object> changes = AuditFieldDiff.compute(before, after);

        assertThat((Object[]) changes.get("patientId")).containsExactly(null, 42L);
    }

    private static Map<String, Object> snapshot(Object... keyValuePairs) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            map.put((String) keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return map;
    }
}

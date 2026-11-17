package uz.uzinfocom.app.platform.audit.domain;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Compares two {@link AuditableFields#auditFields()} snapshots of the same
 * entity (taken before and after a mutation) and returns only the fields
 * whose value actually changed, as {@code fieldName -> [oldValue, newValue]}.
 */
public final class AuditFieldDiff {

    private AuditFieldDiff() {
    }

    public static Map<String, Object> compute(Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> changes = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : after.entrySet()) {
            Object oldValue = before.get(entry.getKey());
            Object newValue = entry.getValue();
            if (!Objects.equals(oldValue, newValue)) {
                changes.put(entry.getKey(), new Object[]{oldValue, newValue});
            }
        }
        return changes;
    }
}

package uz.uzinfocom.app.platform.audit.domain;

import java.util.Map;

/**
 * Implemented by entities that want field-level edits audited (see
 * {@link uz.uzinfocom.app.platform.audit.event.FieldsChangedEvent}). The
 * returned map must contain only immutable scalar values (String, Long,
 * Boolean, enum, LocalDate/Instant, ...) — never a reference to a mutable
 * embedded/child object or a collection. Callers snapshot this map before
 * and after mutating the entity and diff the two; a mutable reference would
 * make both snapshots point at the same already-mutated instance, hiding
 * every change.
 */
public interface AuditableFields {

    Map<String, Object> auditFields();
}

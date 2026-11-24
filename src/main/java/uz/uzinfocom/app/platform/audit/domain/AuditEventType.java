package uz.uzinfocom.app.platform.audit.domain;

public enum AuditEventType {
    CREATED,
    STATUS_CHANGED,
    ORG_REASSIGNED,
    FIELD_CHANGED,
    /** A caller explicitly un-masked a sensitive value (e.g. a patient document number). */
    SENSITIVE_DATA_REVEALED
}

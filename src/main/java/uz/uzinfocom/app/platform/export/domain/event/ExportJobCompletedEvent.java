package uz.uzinfocom.app.platform.export.domain.event;

/**
 * Published once an {@code ExportJob} reaches {@code COMPLETED} — consumed by
 * {@code NotificationEventListener} to tell the requesting user their file is ready to
 * download. Kept as its own event rather than reusing {@code StatusChangedEvent}: export
 * jobs aren't part of the audit trail ({@code AuditEventListener} never sees this event),
 * they always have exactly one recipient (whoever submitted the job) rather than a
 * resolved set, and their lifecycle (PENDING/PROCESSING/COMPLETED/FAILED) has nothing to
 * do with {@code AuditEntityType}'s Form058/Form0581/Card/Act business-status vocabulary.
 */
public record ExportJobCompletedEvent(
        Long jobId,
        Long recipientUserId,
        String exportType,
        String fileName
) {
}

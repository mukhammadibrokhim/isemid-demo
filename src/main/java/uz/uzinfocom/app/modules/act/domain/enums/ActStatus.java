package uz.uzinfocom.app.modules.act.domain.enums;

/**
 * The act's entire lifecycle in one status: created ({@link #NEW}), being
 * filled in by its attached employee(s) ({@link #IN_PROGRESS}), marked
 * ready ({@link #READY}), sent to the external LIS — Laboratory Information
 * System — ({@link #SENT}), and its response received back
 * ({@link #COMPLETED}), which concludes the act. There is no accept/reject
 * or supervisor-approval gate anywhere in this sequence.
 *
 * <p>{@link #SEND_FAILED} is one branch off that line: the LIS call
 * itself failed (network, upstream rejection, malformed response), so the
 * act never reached LIS. It stays editable and re-sendable — the reason is
 * kept in {@code Act.lisInfo.lastError} and the attempt count in
 * {@code Act.lisInfo.attempt}.
 *
 * <p>{@link #RETURNED_BY_LIS} is the other branch: LIS <em>accepted</em> the
 * act ({@link #SENT}) but then, instead of a final result, sent it back for
 * rework via the same callback. Like {@link #SEND_FAILED} it becomes editable
 * and re-sendable again (reason in {@code Act.lisInfo.lastError}, the full
 * callback body in {@code Act.lisInfo.response}) — but unlike it, the act
 * has been seen by LIS, so it can no longer be deleted. Re-sending it
 * creates a fresh request on the LIS side (the frontend passes
 * {@code force: true} on the duplicate {@code senderActNumber}).
 */
public enum ActStatus {
    NEW,
    IN_PROGRESS,
    READY,
    SENT,
    SEND_FAILED,
    RETURNED_BY_LIS,
    COMPLETED
}

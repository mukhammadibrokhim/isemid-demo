package uz.uzinfocom.app.modules.act.domain.enums;

/**
 * The act's entire lifecycle in one status: created ({@link #NEW}), being
 * filled in by its attached employee(s) ({@link #IN_PROGRESS}), marked
 * ready ({@link #READY}), sent to the external LIS — Laboratory Information
 * System — ({@link #SENT}), and its response received back
 * ({@link #COMPLETED}), which concludes the act. There is no accept/reject
 * or supervisor-approval gate anywhere in this sequence.
 *
 * <p>{@link #SEND_FAILED} is the one branch off that line: the LIS call
 * itself failed (network, upstream rejection, malformed response), so the
 * act never reached LIS. It stays editable and re-sendable — the reason is
 * kept in {@code Act.lisInfo.lastError} and the attempt count in
 * {@code Act.lisInfo.attempt}.
 */
public enum ActStatus {
    NEW,
    IN_PROGRESS,
    READY,
    SENT,
    SEND_FAILED,
    COMPLETED
}

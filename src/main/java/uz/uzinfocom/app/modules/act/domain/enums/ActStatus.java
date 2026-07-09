package uz.uzinfocom.app.modules.act.domain.enums;

/**
 * Values preserved from the legacy Act module. Act itself is out of scope
 * for this build — see the Card module plan document — this enum exists
 * only so the {@code Act} placeholder entity can compile and hold a status.
 */
public enum ActStatus {
    NEW,
    IN_PROGRESS,
    COMPLETED,
    NOT_VIEWED,
    ACT_ATTACHED
}

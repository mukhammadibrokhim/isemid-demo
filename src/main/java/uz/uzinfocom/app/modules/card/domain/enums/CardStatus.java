package uz.uzinfocom.app.modules.card.domain.enums;

/**
 * Discriminator values are frontend-facing and preserved from the legacy
 * module. Transition rules below are extracted from the legacy
 * {@code CardServiceImpl} (accept/reject-by-user, complete, supervisor
 * approve/reject) rather than invented — see the plan document for the
 * source trace.
 */
public enum CardStatus {
    NEW,
    IN_PROGRESS,
    COMPLETED,
    ACCEPTED_BY_USER,
    REJECTED_BY_USER,
    APPROVED,
    REJECTED;

    /**
     * Only a fresh, untouched assignment can still be accepted — once the
     * attached user has started actually saving data ({@link #IN_PROGRESS}),
     * accepting again makes no sense, they're already committed to it.
     */
    public boolean canBeAcceptedByUser() {
        return this == NEW;
    }

    /**
     * The attached user may flag the assignment as incorrect only before
     * they've started real work on it ({@link #NEW}/{@link #ACCEPTED_BY_USER}).
     * Once {@link #IN_PROGRESS} — i.e. at least one save has happened —
     * rejecting the assignment is no longer the right action; a supervisor
     * has weighed in (COMPLETED, APPROVED, REJECTED) or the user already
     * rejected it (REJECTED_BY_USER) for the same reason.
     */
    public boolean canBeRejectedByUser() {
        return switch (this) {
            case NEW, ACCEPTED_BY_USER -> true;
            case IN_PROGRESS, REJECTED_BY_USER, COMPLETED, APPROVED, REJECTED -> false;
        };
    }

    /**
     * True while the attached user currently has the ball — right after
     * accepting ({@link #ACCEPTED_BY_USER}), while actively saving progress
     * ({@link #IN_PROGRESS}), or after a supervisor has bounced a completed
     * card back for rework ({@link #REJECTED}). Both editing the card's own
     * fields ({@code update}, which also moves the status to
     * {@link #IN_PROGRESS} on every successful save) and marking it
     * finished ({@code complete}) are gated on exactly this.
     * <p>
     * Deliberately {@code false} for {@link #NEW} (not accepted yet — accept
     * comes first) and {@link #REJECTED_BY_USER} (the attached user
     * themselves declined it; nobody may touch it again until
     * {@link #canBeReassigned()} hands it to a different employee, which
     * resets it back to {@link #NEW}).
     */
    public boolean canBeUpdated() {
        return switch (this) {
            case ACCEPTED_BY_USER, IN_PROGRESS, REJECTED -> true;
            case NEW, REJECTED_BY_USER, COMPLETED, APPROVED -> false;
        };
    }

    /**
     * Deletion is only safe before any real data exists: a fresh
     * assignment ({@link #NEW}/{@link #ACCEPTED_BY_USER}), or one the
     * attached user rejected outright with nothing saved
     * ({@link #REJECTED_BY_USER}). Once at least one save has happened
     * ({@link #IN_PROGRESS}) or the card has moved further along
     * (COMPLETED, APPROVED, REJECTED), there is real work or a real
     * decision on it that deleting would destroy.
     */
    public boolean canBeDeleted() {
        return switch (this) {
            case NEW, ACCEPTED_BY_USER, REJECTED_BY_USER -> true;
            case IN_PROGRESS, COMPLETED, APPROVED, REJECTED -> false;
        };
    }

    /**
     * Only once the attached user has rejected the assignment does it make
     * sense to hand the card to different employees — reassigning resets
     * {@link #NEW} so the newly attached user(s) go through the normal
     * accept/reject cycle themselves.
     */
    public boolean canBeReassigned() {
        return this == REJECTED_BY_USER;
    }

    /**
     * Only a completed card is awaiting the supervisor's decision.
     */
    public boolean canBeApprovedBySupervisor() {
        return this == COMPLETED;
    }

    public boolean canBeRejectedBySupervisor() {
        return this == COMPLETED;
    }

    /**
     * Only {@link #APPROVED} truly ends the workflow. A supervisor
     * rejection ({@link #REJECTED}) is deliberately NOT final — the
     * attached user is expected to fix and re-submit it (see
     * {@link #canBeUpdated()}), so it must stay editable.
     */
    public boolean isFinal() {
        return this == APPROVED;
    }
}

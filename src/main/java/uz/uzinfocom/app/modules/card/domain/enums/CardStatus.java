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
     * The attached user may mark the card as reviewed/correct while it is
     * still new or being worked on.
     */
    public boolean canBeAcceptedByUser() {
        return switch (this) {
            case NEW, IN_PROGRESS -> true;
            case ACCEPTED_BY_USER, REJECTED_BY_USER, COMPLETED, APPROVED, REJECTED -> false;
        };
    }

    /**
     * The attached user may flag the card as incorrect before it has been
     * marked complete. Once a supervisor has weighed in (COMPLETED,
     * APPROVED, REJECTED) or the user already rejected it, this is no
     * longer the right action — see {@link #canBeUpdated()} for how a
     * supervisor rejection gets reworked instead.
     */
    public boolean canBeRejectedByUser() {
        return switch (this) {
            case NEW, IN_PROGRESS, ACCEPTED_BY_USER -> true;
            case REJECTED_BY_USER, COMPLETED, APPROVED, REJECTED -> false;
        };
    }

    /**
     * True while the attached user currently has the ball — after they've
     * accepted the assignment ({@link #ACCEPTED_BY_USER}), or after a
     * supervisor has bounced a completed card back for rework
     * ({@link #REJECTED}). Both editing the card's own fields
     * ({@code update}) and marking it finished ({@code complete}) are
     * gated on exactly this: there's nothing to complete before it could
     * be edited, and nothing worth editing once it's already final or not
     * yet accepted.
     * <p>
     * Deliberately {@code false} for {@link #NEW}/{@link #IN_PROGRESS}
     * (not accepted yet — accept comes first) and
     * {@link #REJECTED_BY_USER} (the attached user themselves declined it;
     * nobody may touch it again until {@link #canBeReassigned()} hands it
     * to a different employee, which resets it back to {@link #NEW}).
     */
    public boolean canBeUpdated() {
        return switch (this) {
            case ACCEPTED_BY_USER, REJECTED -> true;
            case NEW, IN_PROGRESS, REJECTED_BY_USER, COMPLETED, APPROVED -> false;
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

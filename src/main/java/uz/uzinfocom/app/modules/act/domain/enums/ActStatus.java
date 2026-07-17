package uz.uzinfocom.app.modules.act.domain.enums;

/**
 * Mirrors {@link uz.uzinfocom.app.modules.card.domain.enums.CardStatus}'s
 * lifecycle exactly — an Act is assigned blank (like a Card, via
 * {@code assignActs}), the attached employee accepts/rejects it, fills it in
 * through repeated saves, completes it, and a supervisor approves or
 * rejects it. Kept as its own, independent enum rather than reusing
 * {@code CardStatus} — the same convention this codebase already applies to
 * {@code FormStatus}/{@code Form0581Status} (each module owns its own status
 * enum; there is no shared cross-module status package).
 */
public enum ActStatus {
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
     * rejecting the assignment is no longer the right action.
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
     * act back for rework ({@link #REJECTED}).
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
     * ({@link #REJECTED_BY_USER}).
     */
    public boolean canBeDeleted() {
        return switch (this) {
            case NEW, ACCEPTED_BY_USER, REJECTED_BY_USER -> true;
            case IN_PROGRESS, COMPLETED, APPROVED, REJECTED -> false;
        };
    }

    /**
     * Only once the attached user has rejected the assignment does it make
     * sense to hand the act to different employees.
     */
    public boolean canBeReassigned() {
        return this == REJECTED_BY_USER;
    }

    /**
     * Only a completed act is awaiting the supervisor's decision.
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

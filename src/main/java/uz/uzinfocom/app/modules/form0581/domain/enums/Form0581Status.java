package uz.uzinfocom.app.modules.form0581.domain.enums;

public enum Form0581Status {
    SENT,
    ACCEPTED,
    CARD_LINKED,
    APPROVED,
    CANCELED;

    public boolean physicallyDeletable() {
        return switch (this) {
            case SENT -> true;
            case ACCEPTED,
                 CARD_LINKED,
                 APPROVED,
                 CANCELED -> false;
        };
    }

    /**
     * True only while the form is still {@code SENT} — both the sender
     * (withdrawing it) and the receiver (rejecting it) may {@code cancel()}
     * it during this window, see {@code Form0581CancelValidator}. Once the
     * receiver has accepted it, neither side can cancel anymore: the only
     * ways forward from there are approval ({@code APPROVED}) or a
     * super-admin {@code reopen()} of an already-{@code CANCELED} form.
     */
    public boolean isCancellable() {
        return this == SENT;
    }

    /**
     * True only while the form is freshly {@code SENT} and the receiver has
     * not yet decided whether to accept it. Once {@code accept()} moves it
     * on toward approval ({@code ACCEPTED}), that decision cannot be
     * re-made — see {@code Form0581AcceptValidator}.
     */
    public boolean isAcceptanceDecisionPending() {
        return this == SENT;
    }

    /**
     * True once a card has been linked to the form — the sender may only
     * issue the final approval (with the final diagnosis) after that point,
     * never directly from {@code SENT}/{@code ACCEPTED}. See
     * {@code Form0581ApprovalValidator}.
     */
    public boolean isApprovable() {
        return this == CARD_LINKED;
    }

    /**
     * True once a form is {@code CANCELED} — whether that happened via the
     * sender withdrawing it or the receiver rejecting it, both are the same
     * closed/locked outcome. A super admin is the only one who can
     * {@code reopen()} it back to {@code SENT}; see
     * {@code Form0581ReopenValidator}.
     */
    public boolean isReopenable() {
        return this == CANCELED;
    }

    /**
     * True while the case has not yet reached a final outcome (approved or
     * canceled) — drives the "active cases" dashboard metric
     * ({@code Form0581StatsRepository.countActive}).
     */
    public boolean isPending() {
        return switch (this) {
            case SENT, ACCEPTED, CARD_LINKED -> true;
            case APPROVED, CANCELED -> false;
        };
    }
}

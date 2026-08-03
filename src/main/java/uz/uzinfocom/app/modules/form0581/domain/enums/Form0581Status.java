package uz.uzinfocom.app.modules.form0581.domain.enums;

public enum Form0581Status {
    NOT_APPROVED,
    SENT,
    RECEIVED,
    CARD_LINKED,
    APPROVED_PENDING,
    APPROVED,
    CANCELED;

    public boolean physicallyDeletable() {
        return switch (this) {
            case NOT_APPROVED, SENT -> true;
            case RECEIVED,
                 CARD_LINKED,
                 APPROVED_PENDING,
                 APPROVED,
                 CANCELED -> false;
        };
    }

    /**
     * True while the sender's approve/not-approve decision is still open.
     * Once a form has been approved, rejected, or canceled, that decision is
     * final and cannot be re-made.
     */
    public boolean isApprovalDecisionPending() {
        return switch (this) {
            case SENT, RECEIVED, CARD_LINKED, APPROVED_PENDING -> true;
            case NOT_APPROVED, APPROVED, CANCELED -> false;
        };
    }

    /**
     * True only while the form is still {@code SENT} — not yet accepted by
     * the receiver. In that window either party may cancel it: the sender
     * withdraws it, or the receiver declines the incoming "NEW" notification.
     * Once the receiver has accepted it ({@code RECEIVED} and beyond),
     * cancellation is no longer available to either side — only the
     * sender's later approve/not-approve decision remains.
     */
    public boolean isCancellable() {
        return switch (this) {
            case SENT -> true;
            case NOT_APPROVED, RECEIVED, CARD_LINKED, APPROVED_PENDING, APPROVED, CANCELED -> false;
        };
    }
}

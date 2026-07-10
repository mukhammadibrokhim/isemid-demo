package uz.uzinfocom.app.modules.form0581.domain.enums;

public enum Form0581Status {
    NOT_APPROVED,
    SENT,
    RECEIVED,
    APPROVED_PENDING,
    APPROVED,
    CANCELED;

    public boolean physicallyDeletable() {
        return switch (this) {
            case NOT_APPROVED, SENT -> true;
            case RECEIVED,
                 APPROVED_PENDING,
                 APPROVED,
                 CANCELED -> false;
        };
    }

    /**
     * True while the receiver's approve/not-approve decision is still open.
     * Once a form has been approved, rejected, or canceled, that decision is
     * final and cannot be re-made.
     */
    public boolean isApprovalDecisionPending() {
        return switch (this) {
            case SENT, RECEIVED, APPROVED_PENDING -> true;
            case NOT_APPROVED, APPROVED, CANCELED -> false;
        };
    }

    /**
     * True while the sender is still allowed to withdraw the form. Once the
     * receiver has formally approved it, or it was already canceled, it can
     * no longer be canceled.
     */
    public boolean isCancellable() {
        return switch (this) {
            case NOT_APPROVED, SENT, RECEIVED, APPROVED_PENDING -> true;
            case APPROVED, CANCELED -> false;
        };
    }
}

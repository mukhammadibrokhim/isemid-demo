package uz.uzinfocom.app.modules.form129.domain.enums;

public enum Form129Status {
    SENT,
    ACCEPTED,
    CANCELED;

    /**
     * True only while the form is still {@code SENT} — the receiver's
     * accept/reject decision window. See {@code Form129AcceptValidator}/
     * {@code Form129RejectValidator}.
     */
    public boolean isDecisionPending() {
        return this == SENT;
    }
}

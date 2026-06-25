package uz.uzinfocom.app.modules.form058.domain.enums;

public enum FormStatus {
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
}
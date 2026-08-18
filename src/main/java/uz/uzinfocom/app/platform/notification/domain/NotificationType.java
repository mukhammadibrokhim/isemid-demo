package uz.uzinfocom.app.platform.notification.domain;

public enum NotificationType {
    FORM058_RECEIVED,
    FORM058_ACKNOWLEDGED,
    FORM058_CARD_LINKED,
    FORM058_APPROVED,
    FORM058_CANCELED,
    FORM058_REOPENED,
    // Sent to an organization that is neither sender nor receiver, but is
    // the patient's workplace/place-of-study affiliation — see
    // GET /v1/form-058/affiliated and FormAccessScopeResolver.
    FORM058_AFFILIATED_RECEIVED,
    FORM058_AFFILIATED_CARD_LINKED,
    FORM0581_RECEIVED,
    FORM0581_ACKNOWLEDGED,
    FORM0581_CARD_LINKED,
    FORM0581_APPROVED,
    FORM0581_CANCELED,
    FORM0581_REOPENED,
    // Sent to an organization that is neither sender nor receiver, but is
    // the patient's workplace/place-of-study affiliation — see
    // GET /v1/form-058-1/affiliated and FormAccessScopeResolver.
    FORM0581_AFFILIATED_RECEIVED,
    FORM0581_AFFILIATED_CARD_LINKED,
    CARD_ASSIGNED,
    CARD_ACCEPTED_BY_USER,
    CARD_REJECTED_BY_USER,
    CARD_COMPLETED,
    CARD_APPROVED,
    CARD_REJECTED,
    ACT_ASSIGNED,
    ACT_LIS_RESPONSE,
    EXPORT_READY
}

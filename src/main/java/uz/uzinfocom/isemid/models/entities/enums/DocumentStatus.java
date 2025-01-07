package uz.uzinfocom.isemid.models.entities.enums;

import lombok.Getter;

@Getter
public enum DocumentStatus {

    NOT_APPROVED_FORM("Tasdiqlanmagan shakl", "Неутвержденная форма"),
    SENT("Жўнатилди", "Отправлено"),
    RECEIVED_FORM("Қабул қилинган шакл", "Полученная форма"),
    CARD_ATTACHED("Карта бириктирилди", "Прикрепленная карта"),
    APPROVED_FORM_CREATED("Тасдиқланган шакл яратилди", "Подтвержденный форма создана"),
    APPROVED_FORM("Тасдиқланган шакл", "Подтвержденный форма");

    private final String uzbek;
    private final String russian;

    DocumentStatus(String uzbek, String russian) {
        this.uzbek = uzbek;
        this.russian = russian;
    }
}
package uz.uzinfocom.isemid.features.form058.models.enums;

import lombok.Getter;

@Getter
public enum FormStatus {
    NOT_APPROVED("Tasdiqlanmagan", "Неутвержденный"),
    NEW("Yangi", "Новый"),
    SENT("Yuborilgan", "Отправлено"),
    RECEIVED("Qabul qilingan", "Получено"),
    CARD_LINKED("Karta bog‘langan", "Карта прикреплена"),
    CARD_REJECTED("", ""),
    APPROVED_PENDING("Tasdiqlashga tayyor", "Готово к подтверждению"),
    APPROVED("Tasdiqlangan", "Подтвержденный"),
    CANCELED("Bekor qilingan", "Отменено");

    private final String uzbek;
    private final String russian;

    FormStatus(String uzbek, String russian) {
        this.uzbek = uzbek;
        this.russian = russian;
    }
}
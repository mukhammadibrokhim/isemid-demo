package uz.uzinfocom.isemid.models.entities.enums;

import lombok.Getter;

@Getter
public enum FormStatus {
    NOT_APPROVED("Tasdiqlanmagan", "Неутвержденный"),
    SENT("Yuborilgan", "Отправлено"),
    RECEIVED("Qabul qilingan", "Получено"),
    CARD_LINKED("Karta bog‘langan", "Карта прикреплена"),
    APPROVED_PENDING("Tasdiqlashga tayyor", "Готово к подтверждению"),
    APPROVED("Tasdiqlangan", "Подтвержденный"),
    CANCELED("Bekor qilingan", "Отменено"),
    NOT_CONFIRMED_DIAGNOSIS("Berilgan tashxis tasdiqlanmagan", "Выставленный диагноз не подтвержден");


    private final String uzbek;
    private final String russian;

    FormStatus(String uzbek, String russian) {
        this.uzbek = uzbek;
        this.russian = russian;
    }
}
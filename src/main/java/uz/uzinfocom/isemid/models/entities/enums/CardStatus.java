package uz.uzinfocom.isemid.models.entities.enums;

import lombok.Getter;

@Getter
public enum CardStatus {
    NOT_VIEWED("Кўрилмаган", "Не просмотрено"),
    VIEWED("Кўрилди", "Просмотрено"),
    REJECTED("Рад қилинган", "Отклоненный"),
    APPROVED("Тасдиқланган", "Подтвержденный");

    private final String uz;
    private final String ru;

    CardStatus(String uz, String ru) {
        this.uz = uz;
        this.ru = ru;
    }
}

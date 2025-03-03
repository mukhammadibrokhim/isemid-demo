package uz.uzinfocom.isemid.models.entities.card;

import lombok.Getter;

@Getter
public enum CardStatus {
    NOT_VIEWED("Кўрилмаган", "Не просмотрено"),
    VIEWED("Кўрилди", "Просмотрено"),
    REJECTED_BY_USER("Рад қилинган (бириктирилган)", "Отклоненный пользователем"),
    REJECTED_BY_CHECKER("Рад қилинган (текширувчи)", "Отклоненный проверяющим"),
    APPROVED("Тасдиқланган", "Подтвержденный");

    private final String uz;
    private final String ru;

    CardStatus(String uz, String ru) {
        this.uz = uz;
        this.ru = ru;
    }
}

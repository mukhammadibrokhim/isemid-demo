package uz.uzinfocom.isemid.features.card.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CardMessageUtil {

    public static final String UNAUTHORIZED_REJECT = "У вас нет прав для отклонения данной Card.";
    public static final String UNAUTHORIZED_APPROVE = "У вас нет прав для утверждения данной Card.";

    public static final String CARD_ALREADY_APPROVED = "Card уже утверждена.";
    public static final String CARD_ALREADY_COMPLETED = "Card уже завершена.";
    public static final String CARD_NOT_COMPLETED = "Card не завершена.";
    public static final String ONLY_COMPLETED_CAN_BE_REJECTED = "Отклонить можно только завершённые Card.";
    public static final String COMPLETED_CANNOT_BE_REJECTED = "Завершённую Card отклонить невозможно.";

    public static final String REJECTION_REASON_REQUIRED = "Причина отклонения обязательна.";
    public static final String INVALID_CARD_STATUS = "Card не может быть принята в текущем статусе: %s";
    public static final String CARDS_NOT_APPROVED = "Карты не проверены.";

    public static final String CARD_REJECT_SUCCESS = "Card успешно отклонена пользователем.";
}

package uz.uzinfocom.isemid.commons.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MessageUtil {
    public static final String CREATED_SUCCESS = "Создано успешно";
    public static final String UPDATED_SUCCESS = "Успешно обновлено";
    public static final String DELETED_SUCCESS = "Удален успешно";
    public static final String FETCHED_SUCCESS = "Получено успешно";

    // Validation and error messages
    public static final String VALIDATION_ERROR = "Ошибка валидации";
    public static final String INVALID_TYPE = "Недопустимый тип: ожидался %s, но получен %s";

    public static final String NOT_FOUND = "Запрашиваемый ресурс не найден";
    public static final String NOT_FOUND_BY_ID = "Запрашиваемый ресурс с ID = %s не найден";
    public static final String NOT_FOUND_BY_RESOURCE = "Запрашиваемый %s с ID = %s не найден";

    public static final String ALREADY_EXISTS = "Такой объект уже существует";
    public static final String ACCESS_DENIED = "Доступ запрещен";
    public static final String INTERNAL_SERVER_ERROR = "Внутренняя ошибка сервера";
    public static final String BAD_REQUEST = "Некорректный запрос";
    public static final String UNAUTHORIZED = "Неавторизованный доступ";
    //Card
    public static final String CARD_ALREADY_COMPLETED = "Card уже завершена и не может быть изменена.";
    // General messages
    public static final String OPERATION_SUCCESS = "Операция выполнена успешно";
    public static final String OPERATION_FAILED = "Ошибка выполнения операции";
}

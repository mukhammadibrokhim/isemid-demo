package uz.uzinfocom.app.modules.form0581.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус формы №058-1 для табличного представления.")
public enum Form0581TableStatus {
    NEW,
    NOT_APPROVED,
    SENT,
    RECEIVED,
    APPROVED_PENDING,
    APPROVED,
    CANCELED
}

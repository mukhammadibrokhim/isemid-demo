package uz.uzinfocom.app.modules.form058.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус формы №058 для табличного представления.")
public enum Form058TableStatus {
    NEW,
    SENT,
    ACCEPTED,
    CARD_LINKED,
    APPROVED,
    CANCELED
}

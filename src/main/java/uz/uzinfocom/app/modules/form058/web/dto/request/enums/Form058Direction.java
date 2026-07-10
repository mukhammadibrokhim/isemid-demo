package uz.uzinfocom.app.modules.form058.web.dto.request.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Направление списка форм №058 относительно текущей организации: исходящие, "
        + "входящие или все доступные.")
public enum Form058Direction {
    OUTGOING,
    INCOMING,
    ALL
}
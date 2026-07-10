package uz.uzinfocom.app.modules.form0581.web.dto.request.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Направление списка форм №058-1 относительно текущей организации: исходящие, "
        + "входящие или все доступные.")
public enum Form0581Direction {
    OUTGOING,
    INCOMING,
    ALL
}

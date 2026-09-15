package uz.uzinfocom.app.modules.form129.application.query;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Направление списка форм №129 относительно текущей организации: исходящие, "
        + "входящие или все доступные.")
public enum Form129Direction {
    OUTGOING,
    INCOMING,
    ALL
}

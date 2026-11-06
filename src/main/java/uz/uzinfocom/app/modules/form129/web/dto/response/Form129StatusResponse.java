package uz.uzinfocom.app.modules.form129.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;

import java.util.UUID;

@Schema(description = "Результат изменения статуса формы №129 (приём/отклонение).")
public record Form129StatusResponse(
        Long id,
        UUID uuid,
        Form129Status status
) {
}

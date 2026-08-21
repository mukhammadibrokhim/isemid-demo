package uz.uzinfocom.app.modules.form129.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;

import java.util.UUID;

@Schema(description = "Результат создания формы №129.")
public record CreateForm129Response(
        Long id,
        UUID uuid,
        Form129Status status
) {
}

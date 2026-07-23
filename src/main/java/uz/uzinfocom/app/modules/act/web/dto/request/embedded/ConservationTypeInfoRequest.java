package uz.uzinfocom.app.modules.act.web.dto.request.embedded;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Метод консервации пробы.")
public record ConservationTypeInfoRequest(
        Integer conservationMethodId,
        @Size(max = 255) String conservationMethodsUz,
        @Size(max = 255) String conservationMethodsRu
) {
}

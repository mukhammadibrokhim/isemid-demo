package uz.uzinfocom.app.modules.act.web.dto.request.embedded;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Тип упаковки пробы.")
public record PackageTypeInfoRequest(
        Integer packageTypeId,
        @Size(max = 255) String packageTypeUz,
        @Size(max = 255) String packageTypeRu
) {
}

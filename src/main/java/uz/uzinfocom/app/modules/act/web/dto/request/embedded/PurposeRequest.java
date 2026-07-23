package uz.uzinfocom.app.modules.act.web.dto.request.embedded;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Цель отбора пробы.")
public record PurposeRequest(
        Integer purposeId,
        @Size(max = 500) String samplingPurposeUz,
        @Size(max = 500) String samplingPurposeRu,
        @Size(max = 100) String samplingPurposeLoinc
) {
}

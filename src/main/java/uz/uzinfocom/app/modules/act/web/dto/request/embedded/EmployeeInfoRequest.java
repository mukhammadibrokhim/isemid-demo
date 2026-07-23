package uz.uzinfocom.app.modules.act.web.dto.request.embedded;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Сведения о сотруднике, участвовавшем в отборе проб/акте.")
public record EmployeeInfoRequest(
        @Size(max = 255) String fullName,
        Integer positionId,
        @Size(max = 255) String positionUz,
        @Size(max = 255) String positionRu
) {
}

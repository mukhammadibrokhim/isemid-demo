package uz.uzinfocom.app.modules.act.web.dto.request.embedded;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;

@Schema(description = "Сведения о сотруднике, участвовавшем в отборе проб/акте.")
public record EmployeeInfoRequest(
        @Size(max = 255) String fullName,
        Integer positionId,
        @Size(max = 255) String positionUz,
        @Size(max = 255) String positionRu,

        @Schema(description = "Тип идентификатора, по которому данные лица получены из реестра граждан "
                + "(заполняется для участника со стороны проверяемого объекта, не для сотрудника-отборщика).")
        CitizenLookupType identifierType,

        @Size(max = 100) String identifierValue
) {
}

package uz.uzinfocom.app.modules.act.web.dto.request.embedded;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.act.domain.enums.SubjectType;

@Schema(description = "Сведения о субъекте (учреждении/точке/физическом лице), в отношении которого составлен акт.")
public record InstitutionRequest(
        SubjectType subjectType,
        Integer tin,
        @Size(max = 500) String institutionName,
        @Size(max = 500) String institutionAddress,
        @Size(max = 500) String institutionLegalAddress
) {
}

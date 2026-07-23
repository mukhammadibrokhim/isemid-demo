package uz.uzinfocom.app.modules.act.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.web.dto.request.act156.Act156GroupDetailRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.act156.Act156KitchenUtensilRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.InstitutionRequest;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Акт 156 — далолатнома по проверке пищеблока.")
public record Act156Request(
        InstitutionRequest institution,

        @Size(max = 255) String title,
        Integer tin,
        @Size(max = 255) String institutionName,
        @Size(max = 500) String institutionAddress,
        @Size(max = 255) String activityTypeCode,
        LocalDateTime sampleTakenTime,
        Long lisOrganizationId,
        @Size(max = 500) String laboratoryAddress,
        LocalDateTime sampleDeliveryTime,
        @Size(max = 255) String fullNameOfSampler,
        @Size(max = 255) String positionOfSampler,
        @Size(max = 255) String fullNameOfObjectRepresentative,
        @Size(max = 255) String positionOfObjectRepresentative,

        @Valid List<Act156KitchenUtensilRequest> kitchenUtensils,
        @Valid List<Act156GroupDetailRequest> groupDetails
) implements ActRequest {

    public Act156Request {
        kitchenUtensils = kitchenUtensils == null ? List.of() : List.copyOf(kitchenUtensils);
        groupDetails = groupDetails == null ? List.of() : List.copyOf(groupDetails);
    }

    @Override
    public ActType type() {
        return ActType.ACT156;
    }
}

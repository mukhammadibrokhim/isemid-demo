package uz.uzinfocom.app.modules.act.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.web.dto.request.act156.Act156GroupDetailRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.act156.Act156KitchenUtensilRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.ActSubjectRequest;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Акт 156 — далолатнома по проверке пищеблока.")
public record Act156Request(
        @Schema(description = "Субъект акта (блок «Tashkilot turi»).")
        @Valid ActSubjectRequest subject,

        @Size(max = 255) String title,
        @Schema(description = "Вид деятельности («Faoliyat turi») — код из ref_catalog (type=ACTIVITY_TYPE).")
        @Size(max = 255) String activityTypeCode,
        LocalDateTime sampleTakenTime,
        LocalDateTime sampleDeliveryTime,
        @Size(max = 255) String fullNameOfSampler,
        @Size(max = 255) String positionOfSampler,
        @Size(max = 255) String fullNameOfObjectRepresentative,
        @Size(max = 255) String positionOfObjectRepresentative,
        @Schema(description = "Тип документа представителя объекта («Hujjat turi»), по которому его данные получены из реестра граждан.")
        CitizenLookupType identifierTypeOfObjectRepresentative,
        @Schema(description = "Номер документа представителя объекта («Hujjat raqami» — ПИНФЛ или паспорт).")
        @Size(max = 100) String identifierValueOfObjectRepresentative,

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

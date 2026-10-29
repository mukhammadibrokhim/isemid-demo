package uz.uzinfocom.app.modules.act.web.dto.request.act156;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;
import uz.uzinfocom.app.modules.act.web.dto.request.validation.HasPersonIdentifiers;
import uz.uzinfocom.app.modules.act.web.dto.request.validation.PersonIdentifier;
import uz.uzinfocom.app.modules.act.web.dto.request.validation.ValidPersonIdentifiers;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

import java.util.List;

@ValidPersonIdentifiers
@Schema(description = "Сведения о кухонном инвентаре, проверяемые в рамках акта 156.")
public record Act156KitchenUtensilRequest(
        Long id,
        Boolean knifeForBread,
        Boolean fruitCuttingBoard,
        Boolean distributionTable,
        Boolean containerForFinishedProducts,
        @Size(max = 255) String fullNameOfChef,
        @Schema(description = "Тип документа повара («Hujjat turi»), по которому его данные получены из реестра граждан. "
                + "Необязателен, но указывается вместе с identifierValueOfChef.")
        CitizenLookupType identifierTypeOfChef,
        @Schema(description = "Номер документа повара («Hujjat raqami» — ПИНФЛ или паспорт). Для NNUZB — ровно 14 цифр.")
        @Size(max = 100) String identifierValueOfChef,
        Boolean handsOfChef,
        Boolean clothesOfChef
) implements ChildRequest, HasPersonIdentifiers {

    @Override
    public List<PersonIdentifier> personIdentifiers() {
        return List.of(
                new PersonIdentifier("identifierTypeOfChef", identifierTypeOfChef, "identifierValueOfChef", identifierValueOfChef)
        );
    }
}

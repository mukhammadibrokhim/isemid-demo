package uz.uzinfocom.app.modules.card.web.dto.request.card205;

import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.card.web.dto.request.ChildRequest;

import java.time.LocalDateTime;

public record InformationOtherBittenAnimalsRequest(
        Long id,
        @Size(max = 64) String bittenAnimalCategoryCode,
        LocalDateTime bittenDateTime,
        @Size(max = 500) String whereAnimalBitten
) implements ChildRequest {
}

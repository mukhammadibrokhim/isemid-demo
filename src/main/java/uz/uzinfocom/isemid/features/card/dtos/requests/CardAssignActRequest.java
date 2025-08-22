package uz.uzinfocom.isemid.features.card.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.features.act.enums.ActType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardAssignActRequest {
    @NotNull(message = "Card id is required.")
    private Long cardId;
    @NotNull(message = "Act type is required.")
    private List<ActType> actTypes;
}

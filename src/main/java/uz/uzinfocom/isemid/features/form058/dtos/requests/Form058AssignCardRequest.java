package uz.uzinfocom.isemid.features.form058.dtos.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import uz.uzinfocom.isemid.features.card.enums.CardType;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form058AssignCardRequest {
    @NotNull(message = "Form058 id is required.")
    private long form058Id;

    @NotNull(message = "Card type is required.")
    private List<CardType> cardType;

    @Valid
    @NotEmpty
    private List<Long> assignUserIds;
}

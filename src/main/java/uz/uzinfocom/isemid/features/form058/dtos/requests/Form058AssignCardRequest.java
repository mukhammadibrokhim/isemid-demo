package uz.uzinfocom.isemid.features.form058.dtos.requests;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    @Positive(message = "Form058 id must be positive.")
    private Long form058Id;

    @NotEmpty(message = "Card type is required.")
    private List<CardType> cardType;

    @NotEmpty(message = "Assign user ids are required.")
    private List<Long> assignUserIds;
}

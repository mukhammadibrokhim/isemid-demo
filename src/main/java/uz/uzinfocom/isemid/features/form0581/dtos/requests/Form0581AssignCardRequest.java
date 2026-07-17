package uz.uzinfocom.isemid.features.form0581.dtos.requests;

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
public class Form0581AssignCardRequest {

    @NotNull(message = "Form058-1 id is required.")
    @Positive(message = "Form058-1 id must be positive.")
    private Long form0581Id;

    @NotEmpty(message = "Card type is required.")
    private List<CardType> cardType;

    @NotEmpty(message = "Assign user ids are required.")
    private List<Long> assignUserIds;
}

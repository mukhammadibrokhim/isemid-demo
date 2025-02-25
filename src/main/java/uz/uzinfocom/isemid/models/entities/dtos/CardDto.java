package uz.uzinfocom.isemid.models.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.models.entities.card.Card;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardDto {
    private Long id;
    private UUID uuid;
    private String cardType;

    public static CardDto fromEntity(Card card) {
        return CardDto.builder()
                .id(card.getId())
                .uuid(card.getUuid())
                .cardType(card.getCardType().name())
                .build();
    }
}

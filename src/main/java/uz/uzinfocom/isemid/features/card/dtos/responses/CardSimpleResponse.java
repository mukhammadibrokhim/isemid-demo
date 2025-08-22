package uz.uzinfocom.isemid.features.card.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.enums.CardType;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardSimpleResponse {
    private Long id;
    private UUID uuid;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private CardType cardType;
    private CardStatus status;
}

package uz.uzinfocom.isemid.features.card.dtos.requests.card205;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InformationOtherBittenAnimalRequest {
    private Long id;
    private String bittenAnimalCategoryCode;
    private LocalDateTime bittenDateTime;
    private String whereAnimalBitten;
}

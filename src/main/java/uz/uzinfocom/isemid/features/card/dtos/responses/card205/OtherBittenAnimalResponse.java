package uz.uzinfocom.isemid.features.card.dtos.responses.card205;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtherBittenAnimalResponse {
    private Long id;
    private String animalCategoryCode;   // Tishlangan hayvon toifasi kodi
    private LocalDateTime biteDateTime; // Tishlangan vaqt
    private String biteLocation;        // Tishlanish joyi (qayerda tishlangan)
}

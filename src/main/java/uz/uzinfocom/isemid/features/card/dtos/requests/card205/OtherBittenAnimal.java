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
public class OtherBittenAnimal {
    private Long id;
    private String animalCategoryCode;   // Tishlangan hayvon toifasi kodi
    private LocalDateTime biteDateTime; // Tishlangan vaqt
    private String biteLocation;        // Tishlanish joyi (qayerda tishlangan)
}

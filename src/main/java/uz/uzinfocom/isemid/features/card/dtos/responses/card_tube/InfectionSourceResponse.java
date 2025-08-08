package uz.uzinfocom.isemid.features.card.dtos.responses.card_tube;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InfectionSourceResponse {

    private String contactTypeCode; // Sil kasalligi bilan bog‘liq aloqa turi (kod)
    private String fullName; // Aloqa qilingan shaxsning F.I.Sh.
    private String relationCode; // Qarindoshlik darajasi (masalan: ota, ona, qarindosh)
    private String duration; // Aloqa davomiyligi (masalan: 3 oy, 1 yil)
}

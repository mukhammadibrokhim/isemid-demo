package uz.uzinfocom.isemid.features.card.dtos.responses.card174;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutbreakControlMeasureResponse {
    private Integer vaccinatedAnimals;
    private Integer lostAnimals;
    private Integer meatDelivered;
    private String processingMethodCode;//Ishlov berish usuli
    private Integer processedArea;
    private Boolean eventConducted;
}

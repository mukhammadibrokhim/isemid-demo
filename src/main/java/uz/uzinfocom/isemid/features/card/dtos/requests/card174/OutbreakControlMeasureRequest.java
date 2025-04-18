package uz.uzinfocom.isemid.features.card.dtos.requests.card174;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OutbreakControlMeasureRequest {
    private Long id;
    private Integer vaccinatedAnimals;
    private Integer lostAnimals;
    private Integer meatDelivered;
    private String processingMethodCode;
    private Integer processedArea;
    private Boolean eventConducted;
}
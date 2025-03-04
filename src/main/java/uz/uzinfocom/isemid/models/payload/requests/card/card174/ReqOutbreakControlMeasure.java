package uz.uzinfocom.isemid.models.payload.requests.card.card174;

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
public class ReqOutbreakControlMeasure {
    private Long id;
    private Integer vaccinatedAnimals;
    private Integer lostAnimals;
    private Integer meatDelivered;
    private String processingMethodCode;
    private Integer processedArea;
    private Boolean eventConducted;
}
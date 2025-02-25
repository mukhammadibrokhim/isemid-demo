package uz.uzinfocom.isemid.models.payload.requests.card.card161;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqEnvironmentalSource {
    private Long id;
    private String foodAndWaterSourceTypes;
    private String collectionLocation;
    private LocalDateTime collectionTime;
    private String usageLocation;
    private LocalDateTime usageTime;
    private String storageConditions;
    private String qualityFeedbackFromPatientAndOthers;
}

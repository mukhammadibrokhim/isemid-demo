package uz.uzinfocom.isemid.features.card.dtos.responses.card161;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnvironmentalSourceResponse {
    private Long id;
    private String foodAndWaterSourceTypes;
    private String collectionLocation;
    private LocalDateTime collectionTime;
    private String usageLocation;
    private LocalDateTime usageTime;
    private String storageConditions;
    private String qualityFeedbackFromPatientAndOthers;}

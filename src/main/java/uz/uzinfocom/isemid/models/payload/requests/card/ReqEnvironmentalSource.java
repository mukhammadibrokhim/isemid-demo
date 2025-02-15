package uz.uzinfocom.isemid.models.payload.requests.card;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqEnvironmentalSource {
    private String foodAndWaterSourceTypes;
    private String collectionLocationAndTime;
    private String usageLocationAndTime;
    private String storageConditions;
    private String qualityFeedbackFromPatientAndOthers;
}

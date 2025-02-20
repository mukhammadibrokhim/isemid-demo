package uz.uzinfocom.isemid.models.payload.requests.card.card161;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqOutbreakDisinfectionMeasure {
    private Long id;
    private String preventiveMeasuresCode;
    private String drugType;
    private String conductedAt;
    private String conductedLocationCode;
    private String executors;
    private String executionMonitoringResult;
}

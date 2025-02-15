package uz.uzinfocom.isemid.models.payload.requests.card;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqOutbreakDisinfectionMeasure {
    private String preventiveMeasuresCode;
    private String drugType;
    private String conductedAt;
    private String conductedLocationCode;
    private String executors;
    private String executionMonitoringResult;
}

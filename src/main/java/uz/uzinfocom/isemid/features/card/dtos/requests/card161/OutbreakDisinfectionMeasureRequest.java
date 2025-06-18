package uz.uzinfocom.isemid.features.card.dtos.requests.card161;

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
public class OutbreakDisinfectionMeasureRequest {
    private Long id;
    private String preventiveMeasuresCode;
    private String drugType;
    private String conductedAt;
    private String conductedLocationCode;
    private String executors;
    private String executionMonitoringResult;
}

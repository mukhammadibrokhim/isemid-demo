package uz.uzinfocom.isemid.features.card.dtos.responses.card161;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutbreakDisinfectionMeasureResponse {
    private Long id;
    private String preventiveMeasuresCode;
    private String drugType;
    private String conductedAt;
    private String conductedLocationCode;
    private String executors;
    private String executionMonitoringResult;}

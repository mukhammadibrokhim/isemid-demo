package uz.uzinfocom.isemid.features.analytic.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnalyticReportRequest {
    private String title;
    private String payload;
}

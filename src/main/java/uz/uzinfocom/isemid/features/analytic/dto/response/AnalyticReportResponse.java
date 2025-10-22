package uz.uzinfocom.isemid.features.analytic.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnalyticReportResponse {
    private Long id;
    private String title;
    private String payload;
}

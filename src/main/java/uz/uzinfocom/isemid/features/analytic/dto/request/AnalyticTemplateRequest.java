package uz.uzinfocom.isemid.features.analytic.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnalyticTemplateRequest {
    private String name;
    private String content;
}

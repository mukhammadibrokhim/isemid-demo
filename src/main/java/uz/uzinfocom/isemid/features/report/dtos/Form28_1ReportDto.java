package uz.uzinfocom.isemid.features.report.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Form28_1ReportDto {

    private String shortName;
    private String titleUz;
    private String titleUzCyril;
    private String titleRu;
    private String titleKaa;
    private String code;
    private Long totalCount;
    private Long femaleCount;
    private Long age0to17Count;
    private Long age0to14Count;
    private Long under1YearCount;
    private Long age1to2Count;
    private Long age3to5Count;

    private RuralBreakdownDto rural;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RuralBreakdownDto {
        private Long totalCount;
        private Long age0to17Count;
    }
}

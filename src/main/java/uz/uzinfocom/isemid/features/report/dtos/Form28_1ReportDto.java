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

    private String icd10Code;          // ICD-10 code

    private Long totalCases;            // Total registered cases

    // --- "Shu jumladan / including" ---
    private Long femaleCases;           // Among females
    private Long age0to17Cases;         // Children up to 17 (inclusive)
    private Long age0to14Cases;         // Children up to 14 (inclusive)
    private Long under1YearCases;       // Of which: under 1 year
    private Long age1to2Cases;          // Age 1–2 (inclusive)
    private Long age3to5Cases;          // Age 3–5

    private RuralBreakdownDto ruralBreakdown;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RuralBreakdownDto {
        private Long total;        // Total rural population cases
        private Long below17;      // Of which: children aged ≤17 in rural population
    }
}

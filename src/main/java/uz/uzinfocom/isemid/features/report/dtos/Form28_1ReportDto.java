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

    private String icd10Code;      // ICD-10 code

    private Long totalCount;       // ✅ Jami hisob
    private Long femaleCount;      // ✅ Ayollar soni
    private Long age0to17Count;    // ✅ 17 yoshgacha
    private Long age0to14Count;    // ✅ 14 yoshgacha
    private Long under1YearCount;  // ✅ 1 yoshgacha
    private Long age1to2Count;     // ✅ 1–2 yosh
    private Long age3to5Count;     // ✅ 3–5 yosh

    private RuralBreakdownDto rural;  // ✅ Qishloq aholisi statistikasi

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RuralBreakdownDto {
        private Long totalCount;    // ✅ Qishloq aholisi jami
        private Long age0to17Count; // ✅ Qishloqda 17 yoshgacha
    }
}

package uz.uzinfocom.isemid.features.report.dtos;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringResponse {

    private LocalDate diseaseDate; // Kasallik qayd etilgan sana
    private String regionCode; // Hudud kodi
    private String districtCode; // Hudud kodi

    private Integer totalCases;

    private StatusCount status;
    private GenderCount gender;
    private AgeGroupCount ageGroups;

    private DiseaseType diseaseType;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusCount {
        private Integer confirmed;
        private Integer unconfirmed;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenderCount {
        private Integer male;
        private Integer female;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgeGroupCount {
        private int age_0_3;
        private int age_3_6;
        private int age_6_10;
        private int age_10_17;
        private int age_18_25;
        private int age_25_45;
        private int age_45_65;
        private int age_65_plus;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiseaseType {
        private String code;     // MKB-10 kodi
        private String clazz;    // MKB-10 klass
        private String block;    // MKB-10 blok
        private String section;  // MKB-10 bo‘lim
        private String disease;  // Kasallik turi nomi
    }
}

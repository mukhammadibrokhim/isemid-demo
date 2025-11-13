package uz.uzinfocom.isemid.features.report.dtos;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form13ReportDto {
    private String regionName;
    private List<DiseaseStatGroupDto> groups;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DiseaseStatGroupDto {
        private String groupName;
        private List<Form13DiseaseStatDto> diseases;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Form13DiseaseStatDto {
        private String diseaseName;
        private long prevYearAdults;
        private long prevYearChildren;
        private long prevYearTotal;

        private long currYearAdults;
        private long currYearChildren;
        private long currYearTotal;
    }
}

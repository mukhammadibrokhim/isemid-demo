package uz.uzinfocom.isemid.features.report.dtos;

import lombok.*;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form13ReportDto {
    private String locationCode;
    private String locationName;

    private List<DiseaseStatGroupDto> groups;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DiseaseStatGroupDto {
        private String groupName;
        private Form13DiseaseStatDto diseases;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Form13DiseaseStatDto {
        private long prevYearAdults;
        private long prevYearUnder14;
        private long prevYearChildren;
        private long prevYearTotal;

        private long currYearAdults;
        private long currYearUnder14;
        private long currYearChildren;
        private long currYearTotal;
    }
}

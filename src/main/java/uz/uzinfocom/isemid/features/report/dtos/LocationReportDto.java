package uz.uzinfocom.isemid.features.report.dtos;

import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationReportDto {
    private List<ReportDto> reports;
    private int totalConfirmed;
    private int totalUnconfirmed;
    private int totalDocuments;
    /**
     * Level meaning:
     * 1 → Republic
     * 2 → Region (Viloyat)
     * 3 → District/City (Tuman/Shahar)
     * 4 → Neighborhood (Mahalla)
     */
    private int level;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ReportDto {
        private String code;
        private String name;
        private int confirmed;
        private int unconfirmed;
    }
}

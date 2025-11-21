package uz.uzinfocom.isemid.features.report.dtos;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Form6ReportDto {
    private String locationCode;   // Hudud kodi
    private String locationName;   // Hudud nomi

    private Integer previousYearTotal;  // Utgan yil jami
    private Integer currentYearTotal;   // Joriy yil jami
    private Integer growthTotal;        // O‘sish/ kamayish

    private List<AgeGroupRow> rows;     // Yosh guruhlari ro‘yxati


    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgeGroupRow {
        private String ageGroupName;  // "новорожденные", "1-2 года" va h.k.

        private Integer previousYear; // Utgan yil soni
        private Integer currentYear;  // Joriy yil soni
        private Integer growth;       // (current - previous)
    }
}
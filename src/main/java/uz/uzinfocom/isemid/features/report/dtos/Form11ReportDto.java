package uz.uzinfocom.isemid.features.report.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form11ReportDto {
    private String locationCode;   // Hudud kodi
    private String locationName;   // 2 - Hudud nomi

    private Long absPrev;        // 3 - O‘tgan yil absolute
    private Long absCurr;        // 4 - Joriy yil absolute

    private Double intPrev;      // 5 - O‘tgan yil intensity
    private Double intCurr;      // 6 - Joriy yil intensity

    private Double growth;       // 7 - (+/-)

    private Long cityAbs;        // 8
    private Double cityInt;      // 9
    private Double cityPercent;  // 10

    private Long ruralAbs;       // 11
    private Double ruralInt;     // 12
    private Double ruralPercent; // 13

    private Long childAbs;       // 14
    private Double childInt;     // 1
}

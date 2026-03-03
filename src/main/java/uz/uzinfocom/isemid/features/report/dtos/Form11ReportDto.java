package uz.uzinfocom.isemid.features.report.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form11ReportDto {

    private String locationCode;   // 1
    private String locationName;   // 2

    // =========================
    // ABSOLYUT
    // =========================
    private Long absPrev;          // 3 - O‘tgan yil
    private Long absCurr;          // 4 - Joriy yil
    private Double absGrowth;      // NEW - O‘sish / kamayish %

    // =========================
    // INTENSIV
    // =========================
    private Double intPrev;        // 5 - O‘tgan yil
    private Double intCurr;        // 6 - Joriy yil
    private Double intGrowth;      // NEW - O‘sish / kamayish %

    // =========================
    // Shahar
    // =========================
    private Long cityAbs;          // 8
    private Double cityInt;        // 9
    private Double cityPercent;    // 10

    // =========================
    // Qishloq
    // =========================
    private Long ruralAbs;         // 11
    private Double ruralInt;       // 12
    private Double ruralPercent;   // 13

    // =========================
    // Bolalar
    // =========================
    private Long childAbs;         // 14
    private Double childInt;       // 15
}
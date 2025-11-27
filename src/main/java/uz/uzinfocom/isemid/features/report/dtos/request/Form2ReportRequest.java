package uz.uzinfocom.isemid.features.report.dtos.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form2ReportRequest {
    // 1. Registered last year
    private Integer registeredLastYear;

    // 2. Registered this year
    private Integer registeredThisYear;

    // 3. DDU va maktablarda qayd etilgan holatlar
    private Integer casesInSchoolsAndDdu;

    // 4. Dezinfeksiya tadbirlari o‘tkazilgan o‘choqlar soni
    private Integer disinfectionEventsCount;

    // 5. Tekshirilgan obyektlar soni
    private Integer inspectedObjectsCount;

    // 6. Yopilgan / elik qilingan obyektlar soni
    private Integer closedObjectsCount;

    // 7. Shu jumladan — DDU
    private Integer dduCount;

    // 8. Shu jumladan — Maktablar
    private Integer schoolCount;

    // 9. Jarimalar soni
    private Integer finesCount;

    // 10. Prokuraturaga yuborilgan materiallar soni
    private Integer sentToProsecutorCount;

    // 11. Qo‘shimcha izoh
    private String comment;
}

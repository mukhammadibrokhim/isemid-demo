package uz.uzinfocom.isemid.features.report.dtos;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form9ReportDto {

    private String locationCode;     // Hudud/Tuman/Tashkilot kodi
    private String locationName;     // Hudud/Tuman/Tashkilot nomi

    private Total total;             // 12 oy bo‘yicha yillik umumiy natijalar

    private List<Form9RowDto> rows;  // 12 ta oy kesimidagi qatorlar


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Form9RowDto {

        private String monthName;   // Oy nomi (locale bo‘yicha): Yanvar, Fevral, ...

        // === Ro‘yxatga olinganlar (registered) ===
        private Long regCurr;       // Joriy yil oyida ro‘yxatga olinganlar soni
        private Long regPrev;       // O‘tgan yil shu oyda ro‘yxatga olinganlar soni
        private Long regDiff;       // Farqi: regCurr - regPrev

        // === Yotqizilganlar (hospitalized) ===
        private Long hospCurr;      // Joriy yil oyida yotqizilganlar soni
        private Long hospPrev;      // O‘tgan yil shu oyda yotqizilganlar soni
        private Long hospDiff;      // Farqi: hospCurr - hospPrev
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Total {

        // === YILLIK RO‘YXATGA OLGANLAR ===
        private Long regCurrTotal;     // Joriy yil bo‘yicha ro‘yxatga olinganlar jami
        private Long regPrevTotal;     // O‘tgan yil bo‘yicha ro‘yxatga olinganlar jami
        private Long regDiffTotal;     // Yillik farq: regCurrTotal - regPrevTotal

        // === YILLIK YOTQIZILGANLAR ===
        private Long hospCurrTotal;    // Joriy yil bo‘yicha yotqizilganlar jami
        private Long hospPrevTotal;    // O‘tgan yil bo‘yicha yotqizilganlar jami
        private Long hospDiffTotal;    // Yillik farq: hospCurrTotal - hospPrevTotal
    }
}

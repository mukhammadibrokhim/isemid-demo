package uz.uzinfocom.isemid.features.report.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FormReport10Dto {
    /**
     * Hudud kodi (Respublika / Viloyat / Tuman)
     */
    private String locationCode;

    /**
     * Административные территории
     */
    private String locationName;

    /* ===================== CURRENT MONTH ===================== */

    private PeriodStat currentMonth;

    /* ===================== CUMULATIVE ===================== */
    private PeriodStat cumulative;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodStat {

        /**
         * Umumiy aholi
         */
        private Metric total;

        /**
         * 14 yoshgacha bolalar
         */
        private Metric childrenUnder14;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metric {

        /**
         * Абсолютный показатель
         */
        private YearCompare<Integer> absolute;

        /**
         * Интенсивный показатель
         */
        private YearCompare<Double> intensive;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearCompare<T extends Number> {

        // Oldingi yil */
        private T previousYear;

        // Joriy yil */
        private T currentYear;

        /**
         * Рост / снижение %
         */
        private Double growthPercent;
    }
}


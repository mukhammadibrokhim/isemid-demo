package uz.uzinfocom.isemid.features.report.dtos.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form7ReportRequest {
    private Long id;

    private Long organizationId;

    // 2. Кол-во заболеваний на начало периода
    private Integer startPeriodCases;   // Davr boshida kasallanganlar soni

    // 3. Зарегистрировано за отчётный период — Всего
    private Integer registeredTotal;    // Hisobot davrida ro‘yxatga olingan — jami

    // 4. Дети до 18 лет
    private Integer registeredUnder18;  // 18 yoshgacha

    // 5. Взрослые
    private Integer registeredAdults;   // Kattalar

    // 6. Женщины
    private Integer registeredFemale;   // Ayollar

    // 7. Городские жители
    private Integer registeredUrban;    // Shahar aholisi

    // 8. Сельские жители
    private Integer registeredRural;    // Qishloq aholisi

    // 9. Прошло обследование
    private Integer completedExamination; // Tekshiruvdan o‘tgan

    // 10. Подлежит обследованию
    private Integer requiredExamination;  // Tekshiruvdan o‘tishi kerak

    // 11. Подтвержден первичный диагноз
    private Integer confirmedPrimaryDiagnosis; // Birlamchi tashxis tasdiqlangan

    // 12. Госпитализировано
    private Integer hospitalized;       // Shifoxonaga yotqizilgan

    // 13. Кол-во заболеваний на конец периода
    private Integer endPeriodCases;     // Davr oxirida kasallanganlar soni

    // 14. Рост/снижение (+/-)
    private Integer growthRate;         // O‘sish / kamayish
}

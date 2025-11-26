package uz.uzinfocom.isemid.features.report.model;

import jakarta.persistence.Entity;
import lombok.*;
import uz.uzinfocom.isemid.commons.models.AbsEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Form7Report extends AbsEntity {

    // 2. Кол-во заболеваний на начало периода
    private Integer startPeriodCases;

    // 3. Зарегистрировано за отчётный период — Всего
    private Integer registeredTotal;

    // 4. Дети до 18 лет
    private Integer registeredUnder18;

    // 5. Взрослые
    private Integer registeredAdults;

    // 6. Женщины
    private Integer registeredFemale;

    // 7. Городские жители
    private Integer registeredUrban;

    // 8. Сельские жители
    private Integer registeredRural;

    // 9. Прошло обследование
    private Integer completedExamination;

    // 10. Подлежит обследованию
    private Integer requiredExamination;

    // 11. Подтвержден первичный диагноз
    private Integer confirmedPrimaryDiagnosis;

    // 12. Госпитализировано
    private Integer hospitalized;

    // 13. Кол-во заболеваний на конец периода
    private Integer endPeriodCases;     //

    // 14. Рост/снижение (+/-)
    private Integer growthRate;
}

package uz.uzinfocom.isemid.features.report.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.commons.models.AbsEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "report_12")
@EntityListeners(AuditingEntityListener.class)
public class Report12 extends AbsEntity {

    // 1. Регион / Терапевтический отдел СЭБ
    @Column(name = "region_name")
    private String regionName;

    // 2. Кол-во заболеваний на начало периода
    @Column(name = "cases_at_period_start")
    private Integer casesAtPeriodStart;

    // 3. Зарегистрировано больных всего за период
    @Column(name = "registered_total")
    private Integer registeredTotal;

    // 4. Дети до 18 лет
    @Column(name = "registered_children")
    private Integer registeredChildren;

    // 5. Взрослые
    @Column(name = "registered_adults")
    private Integer registeredAdults;

    // 6. Женщины
    @Column(name = "registered_women")
    private Integer registeredWomen;

    // 7. Городские жители
    @Column(name = "registered_urban")
    private Integer registeredUrban;

    // 8. Сельские жители
    @Column(name = "registered_rural")
    private Integer registeredRural;

    // 9. Прошли обследование
    @Column(name = "completed_examination")
    private Integer completedExamination;

    // 10. Подлежат обследованию
    @Column(name = "need_examination")
    private Integer needExamination;

    // 11. Подтвержден диагноз
    @Column(name = "confirmed_diagnosis")
    private Integer confirmedDiagnosis;

    // 12. Госпитализировано
    @Column(name = "hospitalized")
    private Integer hospitalized;

    // 13. Количество заболеваний на конец периода
    @Column(name = "cases_at_period_end")
    private Integer casesAtPeriodEnd;

    // 14. Рост/снижение заболеваемости (+/-)
    @Column(name = "growth_rate")
    private Integer growthRate;
}

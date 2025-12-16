package uz.uzinfocom.isemid.features.report.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import uz.uzinfocom.isemid.commons.models.AbsEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "form_report_32")
public class FormReport32 extends AbsEntity {
    // ============================
    // 1. ПРОВЕРЕННЫЕ ОБЪЕКТЫ
    // ============================
    @Column(name = "inspected_total")
    private Integer inspectedTotal;

    @Column(name = "inspected_ddmt")
    private Integer inspectedDdmt;

    @Column(name = "inspected_school")
    private Integer inspectedSchool;

    @Column(name = "inspected_lpu")
    private Integer inspectedLpu;

    @Column(name = "inspected_other")
    private Integer inspectedOther;


    // ============================
    // 2. ВЫЯВЛЕННЫЕ НЕДОСТАТКИ
    // ============================
    @Column(name = "defects_total")
    private Integer defectsTotal;

    @Column(name = "defects_ddmt")
    private Integer defectsDdmt;

    @Column(name = "defects_school")
    private Integer defectsSchool;

    @Column(name = "defects_lpu")
    private Integer defectsLpu;

    @Column(name = "defects_other")
    private Integer defectsOther;


    // ============================
    // 3. ДОЛЖНОСТНЫЕ ЛИЦА
    // ============================
    @Column(name = "officials_total")
    private Integer officialsTotal;

    @Column(name = "officials_ddmt")
    private Integer officialsDdmt;

    @Column(name = "officials_school")
    private Integer officialsSchool;

    @Column(name = "officials_lpu")
    private Integer officialsLpu;

    @Column(name = "officials_other")
    private Integer officialsOther;


    // ============================
    // 4. ПРИОСТАНОВЛЕННЫЕ ОБЪЕКТЫ
    // ============================
    @Column(name = "suspended_total")
    private Integer suspendedTotal;

    @Column(name = "suspended_ddmt")
    private Integer suspendedDdmt;

    @Column(name = "suspended_school")
    private Integer suspendedSchool;

    @Column(name = "suspended_lpu")
    private Integer suspendedLpu;

    @Column(name = "suspended_other")
    private Integer suspendedOther;
}

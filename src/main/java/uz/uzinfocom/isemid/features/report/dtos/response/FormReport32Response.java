package uz.uzinfocom.isemid.features.report.dtos.response;

import lombok.*;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgMiniResponse;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FormReport32Response {
    private Long id;
    private UUID uuid;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private OrgMiniResponse createdOrg;

    // 1. ПРОВЕРЕННЫЕ ОБЪЕКТЫ
    private Integer inspectedTotal;
    private Integer inspectedDdmt;
    private Integer inspectedSchool;
    private Integer inspectedLpu;
    private Integer inspectedOther;

    // 2. ВЫЯВЛЕННЫЕ НЕДОСТАТКИ
    private Integer defectsTotal;
    private Integer defectsDdmt;
    private Integer defectsSchool;
    private Integer defectsLpu;
    private Integer defectsOther;

    // 3. ДОЛЖНОСТНЫЕ ЛИЦА
    private Integer officialsTotal;
    private Integer officialsDdmt;
    private Integer officialsSchool;
    private Integer officialsLpu;
    private Integer officialsOther;

    // 4. ПРИОСТАНОВЛЕННЫЕ ОБЪЕКТЫ
    private Integer suspendedTotal;
    private Integer suspendedDdmt;
    private Integer suspendedSchool;
    private Integer suspendedLpu;
    private Integer suspendedOther;

}

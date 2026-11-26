package uz.uzinfocom.app.modules.report.shared;

import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.reference.domain.ManualReportAgeUnit;
import uz.uzinfocom.app.modules.reference.domain.ManualReportGender;
import uz.uzinfocom.app.modules.reference.domain.ManualReportCaseSource;
import uz.uzinfocom.app.modules.reference.domain.ManualReportPopulationType;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ReportCaseConditionTest {

    private final Query query = mock(Query.class);

    @Test
    void unrestrictedConditionLeavesTheQueryUntouched() {
        assertThat(ReportCaseCondition.NONE.sqlClause(ManualReportCaseSource.FORM_058)).isEmpty();
        ReportCaseCondition.NONE.bind(query);
        verifyNoInteractions(query);
    }

    @Test
    void upperAgeBoundIsExclusiveAndRejectsBirthAfterRegistration() {
        ReportCaseCondition newborns = new ReportCaseCondition(
                null, null, null, 28, ManualReportAgeUnit.DAY, null, null, null, null, null);

        assertThat(newborns.sqlClause(ManualReportCaseSource.FORM_058))
                .contains("p.birth_date <= f.created_at::date")
                .contains("f.created_at::date < p.birth_date + (:conditionAgeTo) * interval '1 day'")
                .doesNotContain("conditionAgeFrom", "conditionGender");
        newborns.bind(query);
        verify(query).setParameter("conditionAgeTo", 28);
    }

    @Test
    void genderAndBothBoundsAreBoundAsParameters() {
        ReportCaseCondition condition = new ReportCaseCondition(
                ManualReportGender.FEMALE, 1, ManualReportAgeUnit.MONTH, 49, ManualReportAgeUnit.YEAR,
                null, null, null, null, null
        );

        assertThat(condition.sqlClause(ManualReportCaseSource.FORM_058))
                .contains("upper(trim(p.gender_code)) = :conditionGender")
                .contains("f.created_at::date >= p.birth_date + (:conditionAgeFrom) * interval '1 month'")
                .contains("f.created_at::date < p.birth_date + (:conditionAgeTo) * interval '1 year'");
        condition.bind(query);
        verify(query).setParameter("conditionGender", "FEMALE");
        verify(query).setParameter("conditionAgeFrom", 1);
        verify(query).setParameter("conditionAgeTo", 49);
    }

    @Test
    void sourceFormExcludesTheOtherTableButKeepsParametersPresent() {
        ReportCaseCondition only0581 = new ReportCaseCondition(
                ManualReportGender.MALE, null, null, null, null, ManualReportCaseSource.FORM_058_1, null, null, null, null
        );

        assertThat(only0581.sqlClause(ManualReportCaseSource.FORM_058))
                .contains("and false", ":conditionGender");
        assertThat(only0581.sqlClause(ManualReportCaseSource.FORM_058_1))
                .contains(":conditionGender").doesNotContain("and false");
    }

    @Test
    void labConfirmationAndHospitalizationUseEachTablesOwnColumns() {
        ReportCaseCondition condition = new ReportCaseCondition(
                null, null, null, null, null, null, true, ManualReportPopulationType.VILLAGE_RESIDENT, true, null
        );

        assertThat(condition.sqlClause(ManualReportCaseSource.FORM_058))
                .contains("f.lab_confirmation = :conditionLabConfirmation", "f.hospital_place_id is not null",
                        ":conditionPopulationType")
                .doesNotContain("and false", "hospitalized_at");
        assertThat(condition.sqlClause(ManualReportCaseSource.FORM_058_1))
                .contains("and false", "f.hospitalized_at is not null", ":conditionPopulationType")
                .doesNotContain("lab_confirmation", "hospital_place_id");

        condition.bind(query);
        verify(query).setParameter("conditionLabConfirmation", true);
        verify(query).setParameter("conditionPopulationType", "VILLAGE_RESIDENT");
    }

    @Test
    void diseasePlacesRestrictForm058AndExcludeForm0581() {
        ReportCaseCondition schools = new ReportCaseCondition(
                null, null, null, null, null, null, null, null, null, Set.of("IN_SCHOOL", "IN_KINDERGARTEN")
        );

        assertThat(schools.sqlClause(ManualReportCaseSource.FORM_058))
                .contains("f.disease_place_code in (:conditionDiseasePlaces)")
                .doesNotContain("and false");
        assertThat(schools.sqlClause(ManualReportCaseSource.FORM_058_1))
                .contains("and false")
                .doesNotContain("disease_place_code");

        schools.bind(query);
        verify(query).setParameter("conditionDiseasePlaces", Set.of("IN_SCHOOL", "IN_KINDERGARTEN"));
    }

    @Test
    void anEmptyDiseasePlaceSetIsNoRestriction() {
        assertThat(new ReportCaseCondition(null, null, null, null, null, null, null, null, null, Set.of()))
                .isEqualTo(ReportCaseCondition.NONE);
    }

    @Test
    void rowsSharingCodesButNotConditionsAreCountedSeparately() {
        ReportCaseCondition newborns = new ReportCaseCondition(
                null, null, null, 28, ManualReportAgeUnit.DAY, null, null, null, null, null);

        var grouped = ReportRowRule.codesByCondition(Map.of(
                201L, new ReportRowRule(Set.of("P36"), ReportCaseCondition.NONE),
                202L, new ReportRowRule(Set.of("P36"), newborns),
                203L, new ReportRowRule(Set.of("O85"), ReportCaseCondition.NONE)
        ));

        assertThat(grouped).hasSize(2);
        assertThat(grouped.get(ReportCaseCondition.NONE)).containsOnlyKeys(201L, 203L);
        assertThat(grouped.get(newborns)).containsOnlyKeys(202L);
    }

    @Test
    void sumsCodeCountsPerRow() {
        Map<Long, Long> sums = ReportRowRule.sumByEntry(
                Map.of(1L, Set.of("P36", "P38"), 2L, Set.of("A00")),
                Map.of("P36", 3L, "P38", 4L, "H66.0", 9L),
                0L, Long::sum
        );

        assertThat(sums).containsOnly(Map.entry(1L, 7L), Map.entry(2L, 0L));
    }
}

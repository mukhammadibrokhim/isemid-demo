package uz.uzinfocom.app.modules.reference.application.manualreport.command;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.reference.domain.Icd10;
import uz.uzinfocom.app.modules.reference.repository.Icd10Repository;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ManualReportValidatorTest {

    private final Icd10Repository icd10Repository = mock(Icd10Repository.class);
    private final ManualReportValidator validator = new ManualReportValidator(icd10Repository);

    @Test
    void acceptsKnownReportTypesAndCodes() {
        found("A15.0");

        assertThatCode(() -> validator.validate(Set.of("FORM_12"), Set.of("A15.0"), Set.of(), Set.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsUnknownReportType() {
        assertThatThrownBy(() -> validator.validate(Set.of("FORM12"), Set.of("A15.0"), Set.of(), Set.of()))
                .isInstanceOfSatisfying(ManualReportValidationException.class, e -> org.assertj.core.api.Assertions
                        .assertThat(e.getMessageCode())
                        .isEqualTo("reference.manual_report.report_types.unknown"));
    }

    @Test
    void rejectsCodeMissingFromIcd10Reference() {
        found("A15.0");

        assertThatThrownBy(() -> validator.validate(Set.of(), Set.of("A15.0", "ZZZ"), Set.of(), Set.of()))
                .isInstanceOfSatisfying(ManualReportValidationException.class, e -> org.assertj.core.api.Assertions
                        .assertThat(e.getArgs())
                        .containsExactly("ZZZ"));
    }

    @Test
    void keepsAlreadyStoredValuesWithoutRechecking() {
        assertThatCode(() -> validator.validate(
                Set.of("LEGACY_TAG"),
                Set.of("OLD.CODE"),
                Set.of("legacy_tag"),
                Set.of("OLD.CODE")
        )).doesNotThrowAnyException();

        verifyNoInteractions(icd10Repository);
    }

    @Test
    void rejectsEmptyCodeSetLeftAfterNormalization() {
        assertThatThrownBy(() -> validator.validate(Set.of(), Set.of(), Set.of(), Set.of("A15.0")))
                .isInstanceOf(ManualReportValidationException.class);
    }

    private void found(String code) {
        when(icd10Repository.findAllByCodeInAndDeletedFalse(any()))
                .thenReturn(List.of(Icd10.builder().code(code).build()));
    }
}

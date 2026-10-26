package uz.uzinfocom.app.integration.inbound.common.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.integration.inbound.common.exception.InboundValidationException;
import uz.uzinfocom.app.modules.reference.application.lookup.Icd10LookupService;

import java.util.List;
import java.util.Map;

/**
 * Validates the final ICD-10 code of a confirmation against the catalog and fills in the name when the
 * caller did not send one. Reports count confirmed cases by {@code final_icd10_code}, so a code that is
 * not in the catalog must never reach the form.
 */
@Component
@RequiredArgsConstructor
public class InboundFinalDiagnosisResolver {

    private final Icd10LookupService icd10LookupService;

    public ResolvedDiagnosis resolve(String code, String name) {
        String normalizedCode = code == null ? "" : code.trim();
        if (normalizedCode.isEmpty()) {
            throw new InboundValidationException("integration.final-icd10.not-found", code);
        }

        Map<String, String> names = icd10LookupService.resolveNames(List.of(normalizedCode));
        String catalogName = names.get(normalizedCode);
        if (catalogName == null) {
            throw new InboundValidationException("integration.final-icd10.not-found", normalizedCode);
        }

        String resolvedName = StringUtils.hasText(name) ? name.trim() : catalogName;
        return new ResolvedDiagnosis(normalizedCode, resolvedName);
    }

    public record ResolvedDiagnosis(String code, String name) {
    }
}

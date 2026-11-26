package uz.uzinfocom.app.modules.report.shared;

import uz.uzinfocom.app.modules.reference.application.manualreport.query.dto.ManualReportResponse;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/** What one manual-report catalog row counts: its ICD-10 codes under its {@link ReportCaseCondition}. */
public record ReportRowRule(Set<String> icd10Codes, ReportCaseCondition condition) {

    public static ReportRowRule of(ManualReportResponse entry) {
        Set<String> codes = entry.icd10Codes() == null ? Set.of() : entry.icd10Codes().stream()
                .filter(c -> c != null && !c.isBlank())
                .map(c -> c.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
        return new ReportRowRule(codes, ReportCaseCondition.of(entry));
    }

    /** Rows grouped by condition - one database query per key is enough for all its rows. */
    public static Map<ReportCaseCondition, Map<Long, Set<String>>> codesByCondition(Map<Long, ReportRowRule> rules) {
        Map<ReportCaseCondition, Map<Long, Set<String>>> result = new LinkedHashMap<>();
        rules.forEach((entryId, rule) -> result
                .computeIfAbsent(rule.condition(), _ -> new HashMap<>())
                .put(entryId, rule.icd10Codes()));
        return result;
    }

    /** Rolls per-diagnosis-code counts up into one value per row. */
    public static <C> Map<Long, C> sumByEntry(
            Map<Long, Set<String>> codesByEntryId, Map<String, C> countsByCode, C empty, BinaryOperator<C> plus
    ) {
        Map<Long, C> byEntryId = new HashMap<>();
        codesByEntryId.forEach((entryId, codes) -> byEntryId.put(
                entryId,
                codes.stream()
                        .map(code -> countsByCode.getOrDefault(code, empty))
                        .reduce(empty, plus)
        ));
        return byEntryId;
    }
}

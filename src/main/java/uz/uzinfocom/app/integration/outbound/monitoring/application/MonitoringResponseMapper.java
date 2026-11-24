package uz.uzinfocom.app.integration.outbound.monitoring.application;

import org.springframework.stereotype.Component;
import uz.uzinfocom.app.integration.outbound.monitoring.web.dto.MonitoringResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class MonitoringResponseMapper {

    private static final String UNCLASSIFIED = "UNCLASSIFIED";

    public MonitoringResponse toResponse(
            MonitoringFilter filter,
            List<MonitoringLocationRow> locationRows,
            List<MonitoringDiseaseRow> diseaseRows
    ) {
        Map<String, Map<String, Integer>> diseasesByLocation = groupByLocation(diseaseRows);

        List<MonitoringResponse.Item> items = locationRows.stream()
                .map(row -> toItem(row, diseasesByLocation.getOrDefault(row.locationCode(), Map.of())))
                .toList();

        return new MonitoringResponse(filter.to(), items);
    }

    private MonitoringResponse.Item toItem(MonitoringLocationRow row, Map<String, Integer> diseaseType) {
        return new MonitoringResponse.Item(
                row.locationCode(),
                toInt(row.totalCases()),
                toInt(row.confirmed()),
                toInt(row.unconfirmed()),
                toInt(row.male()),
                toInt(row.female()),
                toInt(row.unknownGender()),
                new MonitoringResponse.AgeGroupCount(
                        toInt(row.age0To3()), toInt(row.age4To6()), toInt(row.age7To10()),
                        toInt(row.age11To17()), toInt(row.age18To25()), toInt(row.age26To45()),
                        toInt(row.age46To65()), toInt(row.age66Plus()), toInt(row.unknownAge())
                ),
                diseaseType
        );
    }

    private Map<String, Map<String, Integer>> groupByLocation(List<MonitoringDiseaseRow> rows) {
        Map<String, Map<String, Integer>> result = new LinkedHashMap<>();
        for (MonitoringDiseaseRow row : rows) {
            if (isBlank(row.locationCode()) || isBlank(row.diseaseCode())) {
                continue;
            }
            result.computeIfAbsent(row.locationCode().trim(), ignored -> new LinkedHashMap<>())
                    .merge(diseaseKey(row.diseaseCode()), toInt(row.casesCount()), Math::addExact);
        }
        return result;
    }

    private int toInt(long value) {
        return Math.toIntExact(value);
    }

    /** Block codes are returned in the reference-data shape, {@code (A00-A09)}. */
    private String diseaseKey(String diseaseCode) {
        String code = diseaseCode.trim();
        if (UNCLASSIFIED.equalsIgnoreCase(code)) {
            return UNCLASSIFIED;
        }
        return code.startsWith("(") && code.endsWith(")") ? code : "(" + code + ")";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

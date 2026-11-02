package uz.uzinfocom.app.integration.outbound.monitoring.application;

import org.springframework.stereotype.Component;
import uz.uzinfocom.app.integration.outbound.monitoring.web.dto.MonitoringDynamicsResponse;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Folds the (period, location, disease block) rows into one item per
 * (period, location): counts are summed over the blocks, and every block
 * with a diagnosis gets its own entry in {@code diseaseType}.
 */
@Component
public class MonitoringDynamicsResponseMapper {

    public MonitoringDynamicsResponse toResponse(LocalDate toDate, List<MonitoringDynamicsRow> rows) {
        Map<String, Accumulator> byKey = new LinkedHashMap<>();
        for (MonitoringDynamicsRow row : rows) {
            byKey.computeIfAbsent(row.period() + "|" + row.locationCode(),
                    ignored -> new Accumulator(row.period(), row.locationCode())).add(row);
        }
        return new MonitoringDynamicsResponse(
                toDate, byKey.values().stream().map(Accumulator::toItem).toList());
    }

    /** Block codes are returned in the reference-data shape, {@code (A00-A09)}. */
    static String diseaseKey(String diseaseCode) {
        String code = diseaseCode.trim();
        return code.startsWith("(") && code.endsWith(")") ? code : "(" + code + ")";
    }

    private static final class Accumulator {
        private final String period;
        private final String locationCode;
        private final Map<String, Integer> diseaseType = new LinkedHashMap<>();
        private int confirmed;
        private int unconfirmed;
        private int male;
        private int female;
        private int age0To3;
        private int age3To6;
        private int age6To10;
        private int age10To17;
        private int age18To25;
        private int age25To45;
        private int age45To65;
        private int age65Plus;

        private Accumulator(String period, String locationCode) {
            this.period = period;
            this.locationCode = locationCode;
        }

        private void add(MonitoringDynamicsRow row) {
            int cases = Math.toIntExact(row.confirmed() + row.unconfirmed());
            confirmed = Math.addExact(confirmed, Math.toIntExact(row.confirmed()));
            unconfirmed = Math.addExact(unconfirmed, Math.toIntExact(row.unconfirmed()));
            male = Math.addExact(male, Math.toIntExact(row.male()));
            female = Math.addExact(female, Math.toIntExact(row.female()));
            age0To3 = Math.addExact(age0To3, Math.toIntExact(row.age0To3()));
            age3To6 = Math.addExact(age3To6, Math.toIntExact(row.age4To6()));
            age6To10 = Math.addExact(age6To10, Math.toIntExact(row.age7To10()));
            age10To17 = Math.addExact(age10To17, Math.toIntExact(row.age11To17()));
            age18To25 = Math.addExact(age18To25, Math.toIntExact(row.age18To25()));
            age25To45 = Math.addExact(age25To45, Math.toIntExact(row.age26To45()));
            age45To65 = Math.addExact(age45To65, Math.toIntExact(row.age46To65()));
            age65Plus = Math.addExact(age65Plus, Math.toIntExact(row.age66Plus()));

            if (row.diseaseCode() != null && !row.diseaseCode().isBlank()) {
                diseaseType.merge(diseaseKey(row.diseaseCode()), cases, Math::addExact);
            }
        }

        private MonitoringDynamicsResponse.Item toItem() {
            return new MonitoringDynamicsResponse.Item(
                    period,
                    locationCode,
                    confirmed + unconfirmed,
                    confirmed,
                    unconfirmed,
                    male,
                    female,
                    new MonitoringDynamicsResponse.AgeGroupCount(
                            age0To3, age3To6, age6To10, age10To17,
                            age18To25, age25To45, age45To65, age65Plus),
                    diseaseType
            );
        }
    }
}

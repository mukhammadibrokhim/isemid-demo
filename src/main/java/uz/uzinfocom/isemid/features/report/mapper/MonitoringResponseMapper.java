package uz.uzinfocom.isemid.features.report.mapper;

import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.report.dtos.MonitoringResponse;
import uz.uzinfocom.isemid.features.report.projection.MonitoringDiseaseRow;
import uz.uzinfocom.isemid.features.report.projection.MonitoringLocationRow;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class MonitoringResponseMapper {

    public MonitoringResponse toResponse(
            LocalDate toDate,
            List<MonitoringLocationRow> locationRows,
            List<MonitoringDiseaseRow> diseaseRows
    ) {
        Map<String, Map<String, Integer>> diseaseByLocation =
                groupDiseaseTypesByLocation(diseaseRows);

        MonitoringResponse response =
                new MonitoringResponse();

        response.setToDate(toDate);
        response.setData(
                toLocationItems(
                        locationRows,
                        diseaseByLocation
                )
        );

        return response;
    }

    private List<MonitoringResponse.Item> toLocationItems(
            List<MonitoringLocationRow> rows,
            Map<String, Map<String, Integer>> diseaseByLocation
    ) {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        List<MonitoringResponse.Item> result =
                new ArrayList<>(rows.size());

        for (MonitoringLocationRow row : rows) {
            if (row == null) {
                continue;
            }

            MonitoringResponse.Item item =
                    new MonitoringResponse.Item();

            item.setRegionCode(row.regionCode());

            item.setTotalCases(
                    toIntExact(row.totalCases())
            );

            item.setConfirmed(
                    toIntExact(row.confirmed())
            );

            item.setUnconfirmed(
                    toIntExact(row.unconfirmed())
            );

            item.setMale(
                    toIntExact(row.male())
            );

            item.setFemale(
                    toIntExact(row.female())
            );

            /*
             * New field mapping.
             */
            item.setUnknownGender(
                    toIntExact(row.unknownGender())
            );

            item.setAgeGroups(
                    toAgeGroups(row)
            );

            Map<String, Integer> diseaseType =
                    diseaseByLocation.get(
                            row.regionCode()
                    );

            item.setDiseaseType(
                    diseaseType != null
                            ? new LinkedHashMap<>(diseaseType)
                            : new LinkedHashMap<>()
            );

            result.add(item);
        }

        return result;
    }

    private MonitoringResponse.AgeGroupCount toAgeGroups(
            MonitoringLocationRow row
    ) {
        MonitoringResponse.AgeGroupCount ageGroups =
                new MonitoringResponse.AgeGroupCount();

        /*
         * Existing JSON field names are preserved.
         */
        ageGroups.setAge_0_3(
                toIntExact(row.age0To3())
        );

        ageGroups.setAge_3_6(
                toIntExact(row.age4To6())
        );

        ageGroups.setAge_6_10(
                toIntExact(row.age7To10())
        );

        ageGroups.setAge_10_17(
                toIntExact(row.age11To17())
        );

        ageGroups.setAge_18_25(
                toIntExact(row.age18To25())
        );

        ageGroups.setAge_25_45(
                toIntExact(row.age26To45())
        );

        ageGroups.setAge_45_65(
                toIntExact(row.age46To65())
        );

        ageGroups.setAge_65_plus(
                toIntExact(row.age66Plus())
        );

        /*
         * New field mapping.
         */
        ageGroups.setAge_unknown(
                toIntExact(row.unknownAge())
        );

        return ageGroups;
    }

    private Map<String, Map<String, Integer>>
    groupDiseaseTypesByLocation(
            List<MonitoringDiseaseRow> rows
    ) {
        Map<String, Map<String, Integer>> result =
                new LinkedHashMap<>();

        if (rows == null || rows.isEmpty()) {
            return result;
        }

        for (MonitoringDiseaseRow row : rows) {
            if (!isValidDiseaseRow(row)) {
                continue;
            }

            String locationCode =
                    row.regionCode().trim();

            String diseaseCode =
                    normalizeDiseaseCode(
                            row.diseaseCode()
                    );

            Map<String, Integer> diseaseTypes =
                    result.computeIfAbsent(
                            locationCode,
                            ignored -> new LinkedHashMap<>()
                    );

            diseaseTypes.merge(
                    diseaseCode,
                    toIntExact(row.casesCount()),
                    Math::addExact
            );
        }

        return result;
    }

    private boolean isValidDiseaseRow(
            MonitoringDiseaseRow row
    ) {
        return row != null
                && hasText(row.regionCode())
                && hasText(row.diseaseCode());
    }

    private String normalizeDiseaseCode(
            String diseaseCode
    ) {
        String normalized =
                diseaseCode.trim();

        if ("UNCLASSIFIED"
                .equalsIgnoreCase(normalized)) {
            return "UNCLASSIFIED";
        }

        if (normalized.startsWith("(")
                && normalized.endsWith(")")) {
            return normalized;
        }

        return "(" + normalized + ")";
    }

    private boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }

    private int toIntExact(
            long value
    ) {
        return Math.toIntExact(value);
    }
}
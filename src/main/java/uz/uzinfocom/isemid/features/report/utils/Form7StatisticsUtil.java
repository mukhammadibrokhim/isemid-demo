package uz.uzinfocom.isemid.features.report.utils;

import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;
import uz.uzinfocom.isemid.features.patient.enums.Gender;
import uz.uzinfocom.isemid.features.patient.enums.PopulationType;
import uz.uzinfocom.isemid.features.report.dtos.request.FormReport7Request;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public class Form7StatisticsUtil {

    private Form7StatisticsUtil() {
    }

    // Builds Form 7 field values from a period-and-organization-scoped Form058 list.
    // completedExamination/requiredExamination are left at 0 — Form058 has no reliable
    // examination-status source field today.
    public static FormReport7Request calculate(List<Form058> forms, Integer previousEndPeriodCases) {
        int total = 0;
        int under14 = 0;
        int under18 = 0;
        int adults = 0;
        int females = 0;
        int urban = 0;
        int rural = 0;
        int hospitalized = 0;
        int confirmedPrimaryDiagnosis = 0;

        for (Form058 form : forms) {
            if (form == null || form.getPatient() == null || form.getPatient().getBirthDate() == null) {
                continue;
            }

            total++;

            LocalDate birth = form.getPatient().getBirthDate();
            LocalDate formCreationDate = form.getCreatedAt().toLocalDateTime().toLocalDate();
            int age = Period.between(birth, formCreationDate).getYears();

            if (age < 14) under14++;
            if (age < 18) under18++;
            if (age >= 18) adults++;

            if (Gender.female.name().equalsIgnoreCase(form.getPatient().getGenderCode())) {
                females++;
            }

            String populationType = form.getPatient().getPopulationTypeCode();
            if (PopulationType.CITY_RESIDENT.name().equals(populationType)) {
                urban++;
            } else if (PopulationType.VILLAGE_RESIDENT.name().equals(populationType)) {
                rural++;
            }

            if (form.getHospitalPlaceId() != null && form.getAdmissionDate() != null) {
                hospitalized++;
            }

            if (form.getStatus() == FormStatus.APPROVED) {
                confirmedPrimaryDiagnosis++;
            }
        }

        int startPeriodCases = previousEndPeriodCases != null ? previousEndPeriodCases : 0;
        int endPeriodCases = startPeriodCases + total;
        int growthRate = endPeriodCases - startPeriodCases;

        return new FormReport7Request(
                startPeriodCases,
                total,
                under14,
                under18,
                adults,
                females,
                urban,
                rural,
                0,
                0,
                confirmedPrimaryDiagnosis,
                hospitalized,
                endPeriodCases,
                growthRate
        );
    }
}

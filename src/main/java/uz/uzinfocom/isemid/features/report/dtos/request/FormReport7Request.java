package uz.uzinfocom.isemid.features.report.dtos.request;

public record FormReport7Request(

        Integer startPeriodCases,

        Integer registeredTotal,

        Integer registeredUnder14,

        Integer registeredUnder18,

        Integer registeredAdults,

        Integer registeredFemale,

        Integer registeredUrban,

        Integer registeredRural,

        Integer completedExamination,

        Integer requiredExamination,

        Integer confirmedPrimaryDiagnosis,

        Integer hospitalized,

        Integer endPeriodCases,

        // 14. Рост/снижение (+/-)
        Integer growthRate

) {
}
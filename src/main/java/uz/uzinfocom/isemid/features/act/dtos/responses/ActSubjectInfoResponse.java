package uz.uzinfocom.isemid.features.act.dtos.responses;

public record ActSubjectInfoResponse(
        String subjectType,
        String subjectTin,
        String subjectLegalName,
        String subjectLegalAddress,
        String subjectActualAddress
) {
}
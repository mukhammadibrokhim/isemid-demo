package uz.uzinfocom.isemid.features.act.dtos.responses;

public record ActSubjectInfoResponse(
        String subjectTypeCode,
        String subjectTin,
        String subjectLegalName,
        String subjectLegalAddress,
        String subjectActualAddress
) {
}
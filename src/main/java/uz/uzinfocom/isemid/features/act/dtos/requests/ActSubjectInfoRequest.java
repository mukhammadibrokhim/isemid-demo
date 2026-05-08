package uz.uzinfocom.isemid.features.act.dtos.requests;

public record ActSubjectInfoRequest(
        String subjectTypeCode,
        String subjectTin,
        String subjectLegalName,
        String subjectLegalAddress,
        String subjectActualAddress
) {
}
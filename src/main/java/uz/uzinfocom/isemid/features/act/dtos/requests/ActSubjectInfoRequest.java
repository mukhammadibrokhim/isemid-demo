package uz.uzinfocom.isemid.features.act.dtos.requests;

public record ActSubjectInfoRequest(
        String subjectType,
        String subjectTin,
        String subjectLegalName,
        String subjectLegalAddress,
        String subjectActualAddress
) {
}
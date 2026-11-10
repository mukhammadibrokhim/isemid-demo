package uz.uzinfocom.app.modules.iam.application.shared.dto;

import uz.uzinfocom.app.modules.iam.domain.enums.MedicalType;

/** Shared read projection - organization count grouped by medicalType, for any caller that needs it. */
public record MedicalTypeCountProjection(MedicalType medicalType, long count) {
}

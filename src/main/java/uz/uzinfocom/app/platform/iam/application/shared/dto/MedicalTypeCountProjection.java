package uz.uzinfocom.app.platform.iam.application.shared.dto;

import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;

/** Shared read projection - organization count grouped by medicalType, for any caller that needs it. */
public record MedicalTypeCountProjection(MedicalType medicalType, long count) {
}

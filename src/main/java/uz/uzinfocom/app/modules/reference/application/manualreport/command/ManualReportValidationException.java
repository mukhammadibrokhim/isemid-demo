package uz.uzinfocom.app.modules.reference.application.manualreport.command;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

/** A manual-report create/update carrying report types or ICD-10 codes that cannot be accepted (400). */
public class ManualReportValidationException extends AppException {

    private ManualReportValidationException(String messageCode, Object... args) {
        super(ErrorCode.VALIDATION_FAILED, messageCode, args);
    }

    public static ManualReportValidationException unknownReportTypes(String unknown, String allowed) {
        return new ManualReportValidationException("reference.manual_report.report_types.unknown", unknown, allowed);
    }

    public static ManualReportValidationException unknownIcd10Codes(String unknown) {
        return new ManualReportValidationException("reference.manual_report.icd10_codes.unknown", unknown);
    }

    public static ManualReportValidationException ageUnitMismatch() {
        return new ManualReportValidationException("reference.manual_report.age.unit_mismatch");
    }

    public static ManualReportValidationException ageBoundsOrder() {
        return new ManualReportValidationException("reference.manual_report.age.bounds_order");
    }

    public static ManualReportValidationException labConfirmationUnavailable() {
        return new ManualReportValidationException("reference.manual_report.lab_confirmation.unavailable");
    }

    public static ManualReportValidationException diseasePlaceUnavailable() {
        return new ManualReportValidationException("reference.manual_report.disease_places.unavailable");
    }

    public static ManualReportValidationException unknownDiseasePlaces(String unknown) {
        return new ManualReportValidationException("reference.manual_report.disease_places.unknown", unknown);
    }

    public static ManualReportValidationException icd10CodesRequired() {
        return new ManualReportValidationException("reference.manual_report.icd10_codes.required");
    }
}

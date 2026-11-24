package uz.uzinfocom.app.modules.report.airhealth.application.query;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

/** A request the air-and-health report cannot answer as asked (400): a reversed range or too many buckets. */
public class AirHealthRequestException extends AppException {

    private AirHealthRequestException(String messageCode, Object... args) {
        super(ErrorCode.VALIDATION_FAILED, messageCode, args);
    }

    public static AirHealthRequestException invalidRange() {
        return new AirHealthRequestException("report.air-health.invalid-range");
    }

    public static AirHealthRequestException tooManyBuckets(int max) {
        return new AirHealthRequestException("report.air-health.too-many-buckets", max);
    }
}

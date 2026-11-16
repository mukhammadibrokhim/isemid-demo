package uz.uzinfocom.app.modules.airquality.application.sync;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

/** A second AIR_CONTROL sync was requested while one is still running on this instance. */
public class AirQualitySyncInProgressException extends AppException {

    public AirQualitySyncInProgressException() {
        super(ErrorCode.CONFLICT, "air-quality.sync.in_progress");
    }
}

package uz.uzinfocom.app.platform.devpanel.application.actuator;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

/**
 * A dev-panel actuator request that cannot be served. Carries a stable
 * {@link ErrorCode} so the panel can react on {@code code} instead of message text.
 */
public class DevActuatorException extends AppException {

    private DevActuatorException(ErrorCode errorCode, String messageCode, Object... args) {
        super(errorCode, messageCode, args);
    }

    public static DevActuatorException badRequest(String reason) {
        return new DevActuatorException(ErrorCode.BAD_REQUEST, "dev.actuator.bad-request", reason);
    }

    public static DevActuatorException tooManyStreams(int limit) {
        return new DevActuatorException(ErrorCode.TOO_MANY_REQUESTS, "dev.actuator.streams-exhausted", limit);
    }
}

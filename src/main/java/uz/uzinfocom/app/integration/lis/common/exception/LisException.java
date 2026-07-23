package uz.uzinfocom.app.integration.lis.common.exception;

import lombok.Getter;
import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

/**
 * Base for every failure of a call to the external LIS.
 *
 * <p>Extends {@link AppException} rather than standing up a parallel
 * hierarchy: LIS failures surface through ordinary Act endpoints, so they
 * need the app-wide {@code GlobalExceptionHandler} — i18n resolution, trace
 * id, the standard {@code ApiResponse} envelope — not a package-scoped
 * advice like API2's (whose exceptions only ever escape API2's own
 * controllers).
 *
 * <p>{@link #operation} and the upstream fields exist so a failure can also
 * be recorded verbatim on the act as {@code lisInfo.lastError}; upstream text
 * is always sanitized before it gets here.
 */
@Getter
public class LisException extends AppException {

    private final String operation;
    private final Integer upstreamStatus;
    private final String upstreamMessage;

    protected LisException(
            ErrorCode errorCode,
            String messageCode,
            String operation,
            Integer upstreamStatus,
            String upstreamMessage
    ) {
        super(errorCode, messageCode);
        this.operation = operation;
        this.upstreamStatus = upstreamStatus;
        this.upstreamMessage = upstreamMessage;
    }

    /**
     * One-line, human-readable summary stored in {@code lisInfo.lastError} —
     * what the employee sees in the UI after a failed send.
     */
    public String toShortDescription() {
        StringBuilder builder = new StringBuilder(getErrorCode().getCode());
        if (upstreamStatus != null) {
            builder.append(" (HTTP ").append(upstreamStatus).append(')');
        }
        if (upstreamMessage != null && !upstreamMessage.isBlank()) {
            builder.append(": ").append(upstreamMessage);
        }
        return builder.toString();
    }
}

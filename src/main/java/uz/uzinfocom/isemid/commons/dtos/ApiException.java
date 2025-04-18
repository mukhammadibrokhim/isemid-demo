package uz.uzinfocom.isemid.commons.dtos;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.Nullable;
import org.springframework.web.ErrorResponseException;

import java.util.Locale;

public class ApiException extends ErrorResponseException {
    @Nullable
    private final String reason;

    public ApiException(HttpStatusCode status) {
        this(status, null);
    }

    public ApiException(HttpStatusCode status, @Nullable String reason) {
        this(status, reason, null);
    }

    public ApiException(int rawStatusCode, @Nullable String reason, @Nullable Throwable cause) {
        this(HttpStatusCode.valueOf(rawStatusCode), reason, cause);
    }

    public ApiException(HttpStatusCode status, @Nullable String reason, @Nullable Throwable cause) {
        this(status, reason, cause, null, null);
    }

    protected ApiException(HttpStatusCode status, @Nullable String reason, @Nullable Throwable cause, @Nullable String messageDetailCode, @Nullable Object[] messageDetailArguments) {
        super(status, ProblemDetail.forStatus(status), cause, messageDetailCode, messageDetailArguments);
        this.reason = reason;
        this.setDetail(reason);
    }

    @Nullable
    public String getReason() {
        return this.reason;
    }

    public ProblemDetail updateAndGetBody(@Nullable MessageSource messageSource, Locale locale) {
        super.updateAndGetBody(messageSource, locale);
        if (messageSource != null && this.getReason() != null && this.getReason().equals(this.getBody().getDetail())) {
            Object[] arguments = this.getDetailMessageArguments(messageSource, locale);
            String resolved = messageSource.getMessage(this.getReason(), arguments, null, locale);
            if (resolved != null) {
                this.getBody().setDetail(resolved);
            }
        }

        return this.getBody();
    }

    public String getMessage() {
        HttpStatusCode var10000 = this.getStatusCode();
        return var10000 + (this.reason != null ? " \"" + this.reason + "\"" : "");
    }
}
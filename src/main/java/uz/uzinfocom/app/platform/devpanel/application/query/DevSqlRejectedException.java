package uz.uzinfocom.app.platform.devpanel.application.query;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

/** The dev-panel SQL console refused the statement, or Postgres rejected it; maps to HTTP 400. */
public class DevSqlRejectedException extends AppException {
    public DevSqlRejectedException(String messageCode, Object... args) {
        super(ErrorCode.BAD_REQUEST, messageCode, args);
    }
}

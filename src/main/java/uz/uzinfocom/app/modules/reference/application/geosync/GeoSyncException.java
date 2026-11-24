package uz.uzinfocom.app.modules.reference.application.geosync;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

/**
 * The egov classifier feed could not be fetched / was rejected, or the sync is not configured.
 * Deliberately carries no cause: the HTTP client's exception message embeds the request URL, and
 * that URL carries the access token as a query parameter.
 */
public class GeoSyncException extends AppException {

    /** Upstream failure -> {@code 502}; the reference tables are left untouched. */
    public GeoSyncException(String messageCode) {
        super(ErrorCode.UPSTREAM_ERROR, messageCode);
    }

    public GeoSyncException(ErrorCode errorCode, String messageCode) {
        super(errorCode, messageCode);
    }
}

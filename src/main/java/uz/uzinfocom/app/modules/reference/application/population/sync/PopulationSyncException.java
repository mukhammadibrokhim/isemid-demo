package uz.uzinfocom.app.modules.reference.application.population.sync;

import uz.uzinfocom.app.shared.exception.AppException;
import uz.uzinfocom.app.shared.exception.ErrorCode;

/**
 * The stat.uz SDMX population feed could not be fetched or parsed. Maps to
 * {@code 502} — "the system behind us broke", not "we broke" — so the
 * frontend can tell the admin to retry the sync later, and the reference
 * table is left untouched.
 */
public class PopulationSyncException extends AppException {

    public PopulationSyncException(String messageCode, Throwable cause) {
        super(ErrorCode.UPSTREAM_ERROR, messageCode);
        initCause(cause);
    }

    public PopulationSyncException(String messageCode) {
        super(ErrorCode.UPSTREAM_ERROR, messageCode);
    }
}

package uz.uzinfocom.app.modules.act.application.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.act.application.exception.ActValidationException;
import uz.uzinfocom.app.modules.act.domain.model.embedded.Purpose;
import uz.uzinfocom.app.modules.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.modules.reference.application.lookup.dto.ReferenceItem;

import java.util.Locale;

/**
 * Turns a request's {@code purposeCode} into the act's {@link Purpose}:
 * the code must exist in {@code ref_catalog} under
 * {@link Purpose#CATALOG_TYPE}, and only the (normalized) code is stored —
 * names are resolved on read. Shared by the three handlers whose acts have a
 * purpose (153/154/223).
 */
@Component
@RequiredArgsConstructor
public class ActPurposeResolver {

    private final ReferenceLookupService referenceLookupService;

    /**
     * Same as {@link #resolve(String)}, but a legacy-migrated purpose (only
     * the id/name snapshot, no code) survives a save without a code: the
     * form cannot echo it back as a code, so a missing code there means
     * "not re-picked", not "cleared". Picking any code replaces it.
     */
    public Purpose resolve(String purposeCode, Purpose current) {
        if (!StringUtils.hasText(purposeCode) && isLegacySnapshot(current)) {
            return current;
        }
        return resolve(purposeCode);
    }

    private static boolean isLegacySnapshot(Purpose purpose) {
        return purpose != null
                && purpose.getCode() == null
                && (purpose.getPurposeId() != null || StringUtils.hasText(purpose.getSamplingPurposeUz()));
    }

    public Purpose resolve(String purposeCode) {
        if (!StringUtils.hasText(purposeCode)) {
            return null;
        }
        ReferenceItem item = referenceLookupService.findCatalog(Purpose.CATALOG_TYPE, purposeCode);
        if (item == null) {
            throw new ActValidationException("error.act.purpose-not-found", purposeCode);
        }
        return Purpose.ofCode(item.code() == null ? purposeCode.trim().toUpperCase(Locale.ROOT) : item.code());
    }
}

package uz.uzinfocom.app.modules.act.application.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.act.application.exception.ActValidationException;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.modules.reference.application.lookup.dto.ReferenceItem;

import java.util.Locale;

/**
 * Turns a request's {@code activityTypeCode} («Faoliyat turi») into the code
 * stored on the act: it must exist in {@code ref_catalog} under
 * {@link Act#ACTIVITY_TYPE_CATALOG_TYPE}, and only the (normalized) code is
 * stored — names are resolved on read.
 */
@Component
@RequiredArgsConstructor
public class ActActivityTypeResolver {

    private final ReferenceLookupService referenceLookupService;

    public String resolve(String activityTypeCode) {
        if (!StringUtils.hasText(activityTypeCode)) {
            return null;
        }
        ReferenceItem item = referenceLookupService.findCatalog(Act.ACTIVITY_TYPE_CATALOG_TYPE, activityTypeCode);
        if (item == null) {
            throw new ActValidationException("error.act.activity-type-not-found", activityTypeCode);
        }
        return item.code() == null ? activityTypeCode.trim().toUpperCase(Locale.ROOT) : item.code();
    }
}

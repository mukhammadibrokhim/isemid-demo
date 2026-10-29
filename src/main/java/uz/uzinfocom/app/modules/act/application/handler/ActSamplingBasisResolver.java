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
 * Turns a request's {@code samplingBasisCode} («Namuna olish uchun asos»)
 * into the code stored on the act: it must exist in {@code ref_catalog}
 * under {@link Act#SAMPLING_BASIS_CATALOG_TYPE}, and only the
 * (normalized) code is stored — names are resolved on read.
 */
@Component
@RequiredArgsConstructor
public class ActSamplingBasisResolver {

    private final ReferenceLookupService referenceLookupService;

    public String resolve(String samplingBasisCode) {
        if (!StringUtils.hasText(samplingBasisCode)) {
            return null;
        }
        ReferenceItem item = referenceLookupService.findCatalog(Act.SAMPLING_BASIS_CATALOG_TYPE, samplingBasisCode);
        if (item == null) {
            throw new ActValidationException("error.act.sampling-basis-not-found", samplingBasisCode);
        }
        return item.code() == null ? samplingBasisCode.trim().toUpperCase(Locale.ROOT) : item.code();
    }
}

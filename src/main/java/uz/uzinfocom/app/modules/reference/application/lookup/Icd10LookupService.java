package uz.uzinfocom.app.modules.reference.application.lookup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.reference.domain.Icd10;
import uz.uzinfocom.app.modules.reference.repository.Icd10Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Read-only batch ICD-10 code → localized name lookup, for reports that list
 * results by disease code (e.g. the forecast "top diseases" ranking) and need
 * a display name without pulling in the full {@code Icd10QueryService} tree
 * API. A code with no matching catalog entry is simply absent from the
 * returned map — callers fall back to the raw code.
 */
@Service
@RequiredArgsConstructor
public class Icd10LookupService {

    private final Icd10Repository icd10Repository;
    private final ReferenceNameResolver referenceNameResolver;

    public Map<String, String> resolveNames(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Map.of();
        }

        Map<String, String> names = new HashMap<>(codes.size() * 2);
        for (Icd10 icd10 : icd10Repository.findAllByCodeInAndDeletedFalse(codes)) {
            names.put(icd10.getCode(), referenceNameResolver.resolve(
                    icd10.getNameUz(), icd10.getNameUzCyril(), icd10.getNameRu(), icd10.getNameKaa()
            ));
        }
        return names;
    }
}

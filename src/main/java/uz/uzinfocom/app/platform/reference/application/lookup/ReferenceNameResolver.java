package uz.uzinfocom.app.platform.reference.application.lookup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.platform.reference.application.lookup.dto.ReferenceItem;

@Component
@RequiredArgsConstructor
public class ReferenceNameResolver {

    private final LocalizedTextResolver localizedTextResolver;

    public String resolve(ReferenceItem item) {
        if (item == null) {
            return null;
        }
        return resolve(item.nameUz(), item.nameUzCyril(), item.nameRu(), item.nameKaa());
    }

    public String resolve(
            String nameUz,
            String nameUzCyril,
            String nameRu,
            String nameKaa
    ) {
        return localizedTextResolver.resolve(nameUz, nameUzCyril, nameRu, nameKaa);
    }
}

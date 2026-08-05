package uz.uzinfocom.app.platform.reference.application.catalog.query.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.platform.reference.domain.Catalog;

@Component
@RequiredArgsConstructor
public class CatalogQueryMappingHelper {

    private final LocalizedTextResolver localizedTextResolver;

    @Named("catalogName")
    public String catalogName(Catalog catalog) {
        if (catalog == null) {
            return null;
        }

        return localizedTextResolver.resolve(
                catalog.getNameUz(),
                catalog.getNameUzCyril(),
                catalog.getNameRu(),
                catalog.getNameKaa()
        );
    }
}

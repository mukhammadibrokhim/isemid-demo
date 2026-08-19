package uz.uzinfocom.app.modules.reference.application.neighborhood.query.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.modules.reference.domain.Neighborhood;

@Component
@RequiredArgsConstructor
public class NeighborhoodQueryMappingHelper {

    private final LocalizedTextResolver localizedTextResolver;

    @Named("neighborhoodLookupName")
    public String neighborhoodLookupName(Neighborhood neighborhood) {
        if (neighborhood == null) {
            return null;
        }

        return localizedTextResolver.resolve(
                neighborhood.getNameUz(),
                neighborhood.getNameUzCyril(),
                neighborhood.getNameRu(),
                neighborhood.getNameKaa()
        );
    }
}

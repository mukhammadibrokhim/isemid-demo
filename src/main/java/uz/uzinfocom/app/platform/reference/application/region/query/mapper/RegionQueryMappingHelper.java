package uz.uzinfocom.app.platform.reference.application.region.query.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.platform.reference.domain.Region;

@Component
@RequiredArgsConstructor
public class RegionQueryMappingHelper {

    private final LocalizedTextResolver localizedTextResolver;

    @Named("regionLookupName")
    public String regionLookupName(Region region) {
        if (region == null) {
            return null;
        }

        return localizedTextResolver.resolve(
                region.getNameUz(),
                region.getNameUzCyril(),
                region.getNameRu(),
                region.getNameKaa()
        );
    }
}

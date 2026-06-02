package uz.uzinfocom.app.platform.iam.application.role.query.mapper.helper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.platform.iam.application.role.query.projection.RoleTableProjection;
import uz.uzinfocom.app.platform.iam.domain.Role;

@Component
@RequiredArgsConstructor
public class RoleQueryMappingHelper {

    private final LocalizedTextResolver localizedTextResolver;

    @Named("roleDescription")
    public String roleDescription(Role role) {
        if (role == null) {
            return null;
        }

        return localizedTextResolver.resolve(
                role.getDescriptionUz(),
                role.getDescriptionUzCyril(),
                role.getDescriptionRu(),
                role.getDescriptionKaa()
        );
    }

    @Named("roleProjectionDescription")
    public String roleProjectionDescription(RoleTableProjection projection) {
        if (projection == null) {
            return null;
        }

        return localizedTextResolver.resolve(
                projection.getDescriptionUz(),
                projection.getDescriptionUzCyril(),
                projection.getDescriptionRu(),
                projection.getDescriptionKaa()
        );
    }
}
package uz.uzinfocom.app.modules.act.application.query.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.platform.i18n.MessageResolver;

/**
 * Resolves a locale display name for {@link ActType} — mirrors
 * {@code CardTableMapperHelper}'s one-off exception to the app's default
 * "return raw enum, localize client-side" convention.
 */
@Component
@RequiredArgsConstructor
public class ActTableMapperHelper {

    private final MessageResolver messageResolver;

    @Named("actTypeName")
    public String actTypeName(ActType actType) {
        return actType == null ? null : messageResolver.resolve("act.type." + actType.name());
    }
}

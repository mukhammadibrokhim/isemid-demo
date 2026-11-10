package uz.uzinfocom.app.modules.act.application.query.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.ActSubjectResponse;
import uz.uzinfocom.app.modules.act.application.query.projection.ActTableProjection;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.domain.model.embedded.ActSubject;
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

    /**
     * Rebuilds the embeddable so {@link ActSubject#label()} stays the single
     * definition of the row label; {@code null} when nothing is filled in.
     */
    public ActSubjectResponse subject(ActTableProjection.SubjectRef ref) {
        if (ref == null || (ref.getType() == null && ref.getName() == null && ref.getActualAddress() == null)) {
            return null;
        }
        ActSubject subject = new ActSubject(
                ref.getType(), ref.getTin(), ref.getName(), ref.getLegalAddress(), ref.getActualAddress()
        );
        return new ActSubjectResponse(
                subject.getType(), subject.getTin(), subject.getName(),
                subject.getLegalAddress(), subject.getActualAddress(), subject.label()
        );
    }
}

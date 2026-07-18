package uz.uzinfocom.app.modules.card.application.query.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.platform.i18n.MessageResolver;

/**
 * Resolves locale display names for {@link CardType}/{@link CardStatus} —
 * every other enum in this codebase is returned raw and localized
 * client-side, but the Card table view was explicitly asked to resolve
 * these server-side via {@code card.type.*}/{@code card.status.*} message
 * keys.
 */
@Component
@RequiredArgsConstructor
public class CardTableMapperHelper {

    private final MessageResolver messageResolver;

    @Named("cardTypeName")
    public String cardTypeName(CardType cardType) {
        return cardType == null ? null : messageResolver.resolve("card.type." + cardType.name());
    }

    @Named("cardStatusName")
    public String cardStatusName(CardStatus status) {
        return status == null ? null : messageResolver.resolve("card.status." + status.name());
    }
}

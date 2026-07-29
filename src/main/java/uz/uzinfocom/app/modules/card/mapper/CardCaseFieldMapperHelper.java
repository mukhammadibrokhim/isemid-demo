package uz.uzinfocom.app.modules.card.mapper;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.card.domain.enums.CaseFormType;
import uz.uzinfocom.app.modules.card.domain.model.Card;

/**
 * Resolves "whichever of form058/form0581 owns this card" for the 5 card
 * subtype detail mappers (Card161/174/175/205/CardTubeMapper) — a card
 * belongs to exactly one of the two, never both, so every subtype needs the
 * exact same branch. Mirrors {@code CardTableMapperHelper}'s equivalent
 * methods, which do the same thing for the table-row projection instead of
 * the full entity.
 */
@Component
public class CardCaseFieldMapperHelper {

    @Named("resolveFormId")
    public Long resolveFormId(Card card) {
        if (card.getForm058() != null) {
            return card.getForm058().getId();
        }
        return card.getForm0581() != null ? card.getForm0581().getId() : null;
    }

    @Named("resolveFormType")
    public CaseFormType resolveFormType(Card card) {
        if (card.getForm058() != null) {
            return CaseFormType.FORM058;
        }
        return card.getForm0581() != null ? CaseFormType.FORM0581 : null;
    }
}

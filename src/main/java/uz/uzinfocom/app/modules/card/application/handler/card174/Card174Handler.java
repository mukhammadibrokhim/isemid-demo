package uz.uzinfocom.app.modules.card.application.handler.card174;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.card.application.handler.CardTypeHandler;
import uz.uzinfocom.app.modules.card.application.handler.ChildCollectionSync;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.Card174DetailResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.card174.Card174;
import uz.uzinfocom.app.modules.card.domain.model.card174.InfectionMonitoring;
import uz.uzinfocom.app.modules.card.domain.model.card174.OutbreakControlMeasure;
import uz.uzinfocom.app.modules.card.mapper.card174.Card174Mapper;
import uz.uzinfocom.app.modules.card.web.dto.request.Card174Request;

@Component
@RequiredArgsConstructor
public class Card174Handler implements CardTypeHandler<Card174, Card174Request, Card174DetailResponse> {

    private final Card174Mapper mapper;

    @Override
    public CardType getType() {
        return CardType.CARD174;
    }

    @Override
    public Card174 createBlank() {
        return new Card174();
    }

    @Override
    public void update(Card174 card174, Card174Request request) {
        apply(card174, request);
    }

    @Override
    public void validate(Card174 card174) {
        // No cross-field business rules beyond bean validation identified in
        // the legacy update strategies for this type.
    }

    @Override
    public Card174DetailResponse toResponse(Card174 card174) {
        return mapper.toResponse(card174);
    }

    private void apply(Card174 card174, Card174Request request) {
        mapper.copyOwnFields(card174, request);

        ChildCollectionSync.sync(card174, card174.getInfectionMonitoring(), request.infectionMonitoring(), mapper::toEntity, mapper::update, InfectionMonitoring::setCard174);
        ChildCollectionSync.sync(card174, card174.getOutbreakControlMeasures(), request.outbreakControlMeasures(), mapper::toEntity, mapper::update, OutbreakControlMeasure::setCard174);
    }
}

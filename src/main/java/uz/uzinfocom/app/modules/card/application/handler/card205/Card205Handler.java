package uz.uzinfocom.app.modules.card.application.handler.card205;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.card.application.handler.CardTypeHandler;
import uz.uzinfocom.app.platform.persistence.sync.ChildCollectionSync;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.Card205DetailResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.card205.Card205;
import uz.uzinfocom.app.modules.card.domain.model.card205.InformationAboutAnimaBittenPeople;
import uz.uzinfocom.app.modules.card.domain.model.card205.InformationOtherBittenAnimals;
import uz.uzinfocom.app.modules.card.domain.model.card205.InformationOtherBittenPeople;
import uz.uzinfocom.app.modules.card.mapper.card205.Card205Mapper;
import uz.uzinfocom.app.modules.card.web.dto.request.Card205Request;

@Component
@RequiredArgsConstructor
public class Card205Handler implements CardTypeHandler<Card205, Card205Request, Card205DetailResponse> {

    private final Card205Mapper mapper;

    @Override
    public CardType getType() {
        return CardType.CARD205;
    }

    @Override
    public Card205 createBlank() {
        return new Card205();
    }

    @Override
    public void update(Card205 card205, Card205Request request) {
        apply(card205, request);
    }

    @Override
    public void validate(Card205 card205) {
        // No cross-field business rules beyond bean validation identified in
        // the legacy update strategies for this type.
    }

    @Override
    public Card205DetailResponse toResponse(Card205 card205) {
        return mapper.toResponse(card205);
    }

    private void apply(Card205 card205, Card205Request request) {
        mapper.copyOwnFields(card205, request);

        ChildCollectionSync.sync(card205, card205.getInfoBittenPeople(), request.infoBittenPeople(), mapper::toEntity, mapper::update, InformationOtherBittenPeople::setCard205);
        ChildCollectionSync.sync(card205, card205.getInfoOtherBittenAnimal(), request.infoOtherBittenAnimal(), mapper::toEntity, mapper::update, InformationOtherBittenAnimals::setCard205);
        ChildCollectionSync.sync(card205, card205.getInfoAbtAnimalBittenPeople(), request.infoAbtAnimalBittenPeople(), mapper::toEntity, mapper::update, InformationAboutAnimaBittenPeople::setCard205);
    }
}

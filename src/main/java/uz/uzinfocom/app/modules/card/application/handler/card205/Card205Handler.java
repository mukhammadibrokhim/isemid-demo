package uz.uzinfocom.app.modules.card.application.handler.card205;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.card.application.handler.CardTypeHandler;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.Card205DetailResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.card205.Card205;
import uz.uzinfocom.app.modules.card.domain.model.card205.InformationAboutAnimaBittenPeople;
import uz.uzinfocom.app.modules.card.domain.model.card205.InformationOtherBittenAnimals;
import uz.uzinfocom.app.modules.card.domain.model.card205.InformationOtherBittenPeople;
import uz.uzinfocom.app.modules.card.mapper.card205.Card205Mapper;
import uz.uzinfocom.app.modules.card.web.dto.request.Card205Request;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class Card205Handler implements CardTypeHandler<Card205, Card205Request, Card205DetailResponse> {

    private final Card205Mapper mapper;

    @Override
    public CardType getType() {
        return CardType.CARD205;
    }

    @Override
    public Card205 create(Form058 form, Card205Request request) {
        Card205 card205 = new Card205();
        card205.setForm058(form);
        apply(card205, request);
        return card205;
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

        replaceChildren(card205, card205.getInfoBittenPeople(), request.infoBittenPeople(), mapper::toEntity, InformationOtherBittenPeople::setCard205);
        replaceChildren(card205, card205.getInfoOtherBittenAnimal(), request.infoOtherBittenAnimal(), mapper::toEntity, InformationOtherBittenAnimals::setCard205);
        replaceChildren(card205, card205.getInfoAbtAnimalBittenPeople(), request.infoAbtAnimalBittenPeople(), mapper::toEntity, InformationAboutAnimaBittenPeople::setCard205);
    }

    /**
     * Replaces the contents of a Hibernate-managed {@code @OneToMany}
     * collection in place (never reassigns the field) so
     * {@code orphanRemoval = true} correctly deletes whatever isn't in the
     * new request, while inserts/updates apply to the rest.
     */
    private <E, R> void replaceChildren(
            Card205 card205,
            List<E> managedCollection,
            List<R> requests,
            Function<R, E> toEntity,
            BiConsumer<E, Card205> setParent
    ) {
        managedCollection.clear();
        for (R request : requests) {
            E entity = toEntity.apply(request);
            setParent.accept(entity, card205);
            managedCollection.add(entity);
        }
    }
}

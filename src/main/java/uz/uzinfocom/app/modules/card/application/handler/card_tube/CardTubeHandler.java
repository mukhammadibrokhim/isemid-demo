package uz.uzinfocom.app.modules.card.application.handler.card_tube;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.card.application.handler.CardTypeHandler;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.CardTubeDetailResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.card_tube.CardTube;
import uz.uzinfocom.app.modules.card.domain.model.card_tube.ContactMonitoring;
import uz.uzinfocom.app.modules.card.domain.model.card_tube.InfectionSource;
import uz.uzinfocom.app.modules.card.domain.model.card_tube.TBHistory;
import uz.uzinfocom.app.modules.card.domain.model.card_tube.XRay;
import uz.uzinfocom.app.modules.card.mapper.card_tube.CardTubeMapper;
import uz.uzinfocom.app.modules.card.web.dto.request.CardTubeRequest;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class CardTubeHandler implements CardTypeHandler<CardTube, CardTubeRequest, CardTubeDetailResponse> {

    private final CardTubeMapper mapper;

    @Override
    public CardType getType() {
        return CardType.CARD_TUBE;
    }

    @Override
    public CardTube create(Form058 form, CardTubeRequest request) {
        CardTube cardTube = new CardTube();
        cardTube.setForm058(form);
        apply(cardTube, request);
        return cardTube;
    }

    @Override
    public CardTube createBlank() {
        return new CardTube();
    }

    @Override
    public void update(CardTube cardTube, CardTubeRequest request) {
        apply(cardTube, request);
    }

    @Override
    public void validate(CardTube cardTube) {
        // No cross-field business rules beyond bean validation identified in
        // the legacy update strategies for this type.
    }

    @Override
    public CardTubeDetailResponse toResponse(CardTube cardTube) {
        return mapper.toResponse(cardTube);
    }

    private void apply(CardTube cardTube, CardTubeRequest request) {
        mapper.copyOwnFields(cardTube, request);

        replaceChildren(cardTube, cardTube.getPreMBTChestXRay(), request.preMBTChestXRay(), mapper::toEntity, XRay::setCardTube);
        replaceChildren(cardTube, cardTube.getPreviousTBHistory(), request.previousTBHistory(), mapper::toEntity, TBHistory::setCardTube);
        replaceChildren(cardTube, cardTube.getPossibleInfectionSources(), request.possibleInfectionSources(), mapper::toEntity, InfectionSource::setCardTube);
        replaceChildren(cardTube, cardTube.getContactMonitoringList(), request.contactMonitoringList(), mapper::toEntity, ContactMonitoring::setCardTube);
    }

    /**
     * Replaces the contents of a Hibernate-managed {@code @OneToMany}
     * collection in place (never reassigns the field) so
     * {@code orphanRemoval = true} correctly deletes whatever isn't in the
     * new request, while inserts/updates apply to the rest.
     */
    private <E, R> void replaceChildren(
            CardTube cardTube,
            List<E> managedCollection,
            List<R> requests,
            Function<R, E> toEntity,
            BiConsumer<E, CardTube> setParent
    ) {
        managedCollection.clear();
        for (R request : requests) {
            E entity = toEntity.apply(request);
            setParent.accept(entity, cardTube);
            managedCollection.add(entity);
        }
    }
}

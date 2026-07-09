package uz.uzinfocom.app.modules.card.application.handler.card_tube;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.card.application.handler.CardTypeHandler;
import uz.uzinfocom.app.modules.card.application.handler.ChildCollectionSync;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.CardTubeDetailResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.card_tube.CardTube;
import uz.uzinfocom.app.modules.card.domain.model.card_tube.ContactMonitoring;
import uz.uzinfocom.app.modules.card.domain.model.card_tube.InfectionSource;
import uz.uzinfocom.app.modules.card.domain.model.card_tube.TBHistory;
import uz.uzinfocom.app.modules.card.domain.model.card_tube.XRay;
import uz.uzinfocom.app.modules.card.mapper.card_tube.CardTubeMapper;
import uz.uzinfocom.app.modules.card.web.dto.request.CardTubeRequest;

@Component
@RequiredArgsConstructor
public class CardTubeHandler implements CardTypeHandler<CardTube, CardTubeRequest, CardTubeDetailResponse> {

    private final CardTubeMapper mapper;

    @Override
    public CardType getType() {
        return CardType.CARD_TUBE;
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

        ChildCollectionSync.sync(cardTube, cardTube.getPreMBTChestXRay(), request.preMBTChestXRay(), mapper::toEntity, mapper::update, XRay::setCardTube);
        ChildCollectionSync.sync(cardTube, cardTube.getPreviousTBHistory(), request.previousTBHistory(), mapper::toEntity, mapper::update, TBHistory::setCardTube);
        ChildCollectionSync.sync(cardTube, cardTube.getPossibleInfectionSources(), request.possibleInfectionSources(), mapper::toEntity, mapper::update, InfectionSource::setCardTube);
        ChildCollectionSync.sync(cardTube, cardTube.getContactMonitoringList(), request.contactMonitoringList(), mapper::toEntity, mapper::update, ContactMonitoring::setCardTube);
    }
}

package uz.uzinfocom.app.modules.card.application.handler.card161;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.card.application.handler.CardTypeHandler;
import uz.uzinfocom.app.platform.persistence.sync.ChildCollectionSync;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.Card161DetailResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.card161.Card161;
import uz.uzinfocom.app.modules.card.domain.model.card161.Card161RiskFactor;
import uz.uzinfocom.app.modules.card.domain.model.card161.ContactPerson;
import uz.uzinfocom.app.modules.card.domain.model.card161.EnvironmentalLabTest;
import uz.uzinfocom.app.modules.card.domain.model.card161.EnvironmentalSource;
import uz.uzinfocom.app.modules.card.domain.model.card161.HomePreventiveMeasure;
import uz.uzinfocom.app.modules.card.domain.model.card161.InfectionSource;
import uz.uzinfocom.app.modules.card.domain.model.card161.InfectionSourceDetail;
import uz.uzinfocom.app.modules.card.domain.model.card161.OutbreakDisinfectionMeasure;
import uz.uzinfocom.app.modules.card.domain.model.card161.ScreenedGroup;
import uz.uzinfocom.app.modules.card.domain.model.card161.Vaccination;
import uz.uzinfocom.app.modules.card.mapper.card161.Card161Mapper;
import uz.uzinfocom.app.modules.card.web.dto.request.Card161Request;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.InfectionSourceDetailRequest;
import uz.uzinfocom.app.platform.iam.domain.Organization;

@Component
@RequiredArgsConstructor
public class Card161Handler implements CardTypeHandler<Card161, Card161Request, Card161DetailResponse> {

    private final Card161Mapper mapper;
    private final EntityManager entityManager;

    @Override
    public CardType getType() {
        return CardType.CARD161;
    }

    @Override
    public Card161 createBlank() {
        return new Card161();
    }

    @Override
    public void update(Card161 card161, Card161Request request) {
        apply(card161, request);
    }

    @Override
    public void validate(Card161 card161) {
        // No cross-field business rules beyond bean validation identified in
        // the legacy update strategies for this type.
    }

    @Override
    public Card161DetailResponse toResponse(Card161 card161) {
        return mapper.toResponse(card161);
    }

    private void apply(Card161 card161, Card161Request request) {
        mapper.copyOwnFields(card161, request);

        card161.setPolyclinic(resolveReference(Organization.class, request.polyclinicId()));

        ChildCollectionSync.sync(card161, card161.getVaccinations(), request.vaccinations(), mapper::toEntity, mapper::update, Vaccination::setCard161);
        ChildCollectionSync.sync(card161, card161.getRiskFactors(), request.riskFactors(), mapper::toEntity, mapper::update, Card161RiskFactor::setCard161);
        ChildCollectionSync.sync(card161, card161.getPossibleInfectionSources(), request.possibleInfectionSources(), mapper::toEntity, mapper::update, InfectionSource::setCard161);
        ChildCollectionSync.sync(card161, card161.getEnvironmentalSources(), request.environmentalSources(), mapper::toEntity, mapper::update, EnvironmentalSource::setCard161);
        ChildCollectionSync.sync(card161, card161.getEnvironmentalLabTests(), request.environmentalLabTests(), mapper::toEntity, mapper::update, EnvironmentalLabTest::setCard161);
        ChildCollectionSync.sync(card161, card161.getContactPersonDetails(), request.contactPersonDetails(), mapper::toEntity, mapper::update, ContactPerson::setCard161);
        ChildCollectionSync.sync(card161, card161.getScreenedGroups(), request.screenedGroups(), mapper::toEntity, mapper::update, ScreenedGroup::setCard161);
        ChildCollectionSync.sync(card161, card161.getHomePreventiveMeasures(), request.homePreventiveMeasures(), mapper::toEntity, mapper::update, HomePreventiveMeasure::setCard161);
        ChildCollectionSync.sync(card161, card161.getOutbreakDisinfectionMeasures(), request.outbreakDisinfectionMeasures(), mapper::toEntity, mapper::update, OutbreakDisinfectionMeasure::setCard161);

        applyInfectionSourceDetail(card161, request.infectionSourceDetail());
    }

    private void applyInfectionSourceDetail(Card161 card161, InfectionSourceDetailRequest request) {
        if (request == null) {
            card161.setInfectionSourceDetail(null);
            return;
        }

        InfectionSourceDetail detail = card161.getInfectionSourceDetail();
        if (detail == null) {
            detail = new InfectionSourceDetail();
            detail.setCard161(card161);
        }

        detail.setInfectionSourceNotFoundCode(request.infectionSourceNotFoundCode());
        detail.setPersonFullName(request.personFullName());
        detail.setInfectionSourceDiseasePeriodCode(request.infectionSourceDiseasePeriodCode());
        detail.setAnimalTypeCode(request.animalTypeCode());

        card161.setInfectionSourceDetail(detail);
    }

    private <T> T resolveReference(Class<T> type, Long id) {
        return id == null ? null : entityManager.getReference(type, id);
    }
}

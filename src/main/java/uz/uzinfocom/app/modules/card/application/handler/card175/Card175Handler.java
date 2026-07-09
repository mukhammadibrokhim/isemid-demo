package uz.uzinfocom.app.modules.card.application.handler.card175;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.card.application.handler.CardTypeHandler;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.Card175DetailResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.card175.Card175;
import uz.uzinfocom.app.modules.card.mapper.card175.Card175Mapper;
import uz.uzinfocom.app.modules.card.web.dto.request.Card175Request;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;

@Component
@RequiredArgsConstructor
public class Card175Handler implements CardTypeHandler<Card175, Card175Request, Card175DetailResponse> {

    private final Card175Mapper mapper;

    @Override
    public CardType getType() {
        return CardType.CARD175;
    }

    @Override
    public Card175 create(Form058 form, Card175Request request) {
        Card175 card175 = new Card175();
        card175.setForm058(form);
        mapper.copyOwnFields(card175, request);
        return card175;
    }

    @Override
    public Card175 createBlank() {
        return new Card175();
    }

    @Override
    public void update(Card175 card175, Card175Request request) {
        mapper.copyOwnFields(card175, request);
    }

    @Override
    public void validate(Card175 card175) {
        // No cross-field business rules beyond bean validation identified in
        // the legacy update strategies for this type.
    }

    @Override
    public Card175DetailResponse toResponse(Card175 card175) {
        return mapper.toResponse(card175);
    }
}

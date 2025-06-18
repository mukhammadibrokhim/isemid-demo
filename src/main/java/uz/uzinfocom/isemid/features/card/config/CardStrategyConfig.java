package uz.uzinfocom.isemid.features.card.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.strategy.UpdateCardStrategy;
import uz.uzinfocom.isemid.features.card.strategy.impls.*;

import java.util.Map;

@Configuration
public class CardStrategyConfig {

    @Bean
    public Map<CardType, UpdateCardStrategy> updateCardStrategies(
            UpdateCard161Strategy updateCard161Strategy,
            UpdateCard174Strategy updateCard174Strategy,
            UpdateCard175Strategy updateCard175Strategy,
            UpdateCard205Strategy updateCard205Strategy,
            UpdateCardTubeStrategy updateCardTubeStrategy
    ) {
        return Map.of(
                CardType.CARD161, updateCard161Strategy,
                CardType.CARD174, updateCard174Strategy,
                CardType.CARD175, updateCard175Strategy,
                CardType.CARD205, updateCard205Strategy,
                CardType.CARD_TUBE, updateCardTubeStrategy
        );
    }
}
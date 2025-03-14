package uz.uzinfocom.isemid.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uz.uzinfocom.isemid.models.entities.card.CardType;
import uz.uzinfocom.isemid.models.entities.enums.ActType;
import uz.uzinfocom.isemid.services.act.update.UpdateActStrategy;
import uz.uzinfocom.isemid.services.card.strategy.UpdateCardStrategy;
import uz.uzinfocom.isemid.services.impls.act.update.UpdateActs154Strategy;
import uz.uzinfocom.isemid.services.impls.card.strategy.*;

import java.util.Map;

@Configuration
public class StrategyConfig {
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
    @Bean
    public Map<ActType, UpdateActStrategy> updateActStrategyMap(
            UpdateActs154Strategy updateActs154Strategy
    ) {
        return Map.of(
                ActType.ACT154, updateActs154Strategy
        );
    }

}
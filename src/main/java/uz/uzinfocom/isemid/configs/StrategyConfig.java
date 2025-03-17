package uz.uzinfocom.isemid.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uz.uzinfocom.isemid.models.entities.card.CardType;
import uz.uzinfocom.isemid.models.entities.enums.ActType;
import uz.uzinfocom.isemid.services.act.strategy.UpdateActStrategy;
import uz.uzinfocom.isemid.services.card.strategy.UpdateCardStrategy;
import uz.uzinfocom.isemid.services.impls.act.strategy.*;
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
            UpdateAct153Strategy updateAct153Strategy,
            UpdateAct154Strategy updateActs154Strategy,
            UpdateAct155Strategy updateAct155Strategy,
            UpdateAct156Strategy updateAct156Strategy,
            UpdateAct223Strategy updateAct223Strategy,
            UpdateAct224Strategy updateAct224Strategy
    ) {
        return Map.of(
                ActType.ACT153, updateAct153Strategy,
                ActType.ACT154, updateActs154Strategy,
                ActType.ACT155, updateAct155Strategy,
                ActType.ACT156, updateAct156Strategy,
                ActType.ACT223, updateAct223Strategy,
                ActType.ACT224, updateAct224Strategy
        );
    }

}
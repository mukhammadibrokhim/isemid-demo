package uz.uzinfocom.isemid.features.act.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uz.uzinfocom.isemid.features.act.enums.ActType;
import uz.uzinfocom.isemid.features.act.strategy.UpdateActStrategy;
import uz.uzinfocom.isemid.features.act.strategy.impls.*;

import java.util.Map;

@Configuration
public class ActStrategyConfig {
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



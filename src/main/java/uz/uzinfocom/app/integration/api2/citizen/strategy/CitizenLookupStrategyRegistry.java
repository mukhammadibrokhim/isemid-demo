package uz.uzinfocom.app.integration.api2.citizen.strategy;

import org.springframework.stereotype.Component;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;
import uz.uzinfocom.app.integration.api2.citizen.exception.UnsupportedCitizenLookupTypeException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class CitizenLookupStrategyRegistry {

    private final Map<CitizenLookupType, CitizenLookupStrategy> strategies;

    public CitizenLookupStrategyRegistry(List<CitizenLookupStrategy> strategies) {
        Map<CitizenLookupType, CitizenLookupStrategy> mappedStrategies =
                new EnumMap<>(CitizenLookupType.class);

        for (CitizenLookupStrategy strategy : strategies) {
            mappedStrategies.put(strategy.type(), strategy);
        }

        this.strategies = Map.copyOf(mappedStrategies);
    }

    public CitizenLookupStrategy getRequired(CitizenLookupType type) {
        CitizenLookupStrategy strategy = strategies.get(type);

        if (strategy == null) {
            throw new UnsupportedCitizenLookupTypeException();
        }

        return strategy;
    }
}

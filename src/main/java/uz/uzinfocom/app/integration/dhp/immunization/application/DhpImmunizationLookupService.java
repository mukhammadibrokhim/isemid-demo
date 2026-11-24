package uz.uzinfocom.app.integration.dhp.immunization.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.integration.dhp.common.support.DhpNiValidator;
import uz.uzinfocom.app.integration.dhp.immunization.application.mapper.DhpImmunizationMapper;
import uz.uzinfocom.app.integration.dhp.immunization.client.DhpImmunizationClient;
import uz.uzinfocom.app.integration.dhp.immunization.web.dto.ImmunizationResponse;

@Service
@RequiredArgsConstructor
public class DhpImmunizationLookupService {

    private final DhpImmunizationClient client;

    public ImmunizationResponse lookupByNi(String rawNi) {
        String ni = DhpNiValidator.validate(rawNi);

        return new ImmunizationResponse(ni, DhpImmunizationMapper.map(client.searchByNi(ni)));
    }
}

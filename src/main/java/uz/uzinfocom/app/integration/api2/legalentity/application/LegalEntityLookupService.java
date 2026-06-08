package uz.uzinfocom.app.integration.api2.legalentity.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.integration.api2.legalentity.client.LegalEntityApi2Client;
import uz.uzinfocom.app.integration.api2.legalentity.domain.LegalEntityLookupResult;
import uz.uzinfocom.app.integration.api2.legalentity.validation.LegalEntityLookupValidator;

@Service
@RequiredArgsConstructor
public class LegalEntityLookupService {

    private final LegalEntityLookupValidator validator;
    private final LegalEntityApi2Client legalEntityApi2Client;

    public LegalEntityLookupResult lookupByTin(String tin) {
        String validatedTin = validator.validateTin(tin);

        return legalEntityApi2Client.lookupByTin(validatedTin);
    }
}

package uz.uzinfocom.isemid.features.form0581.mappers;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Context;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.form0581.models.Form0581;
import uz.uzinfocom.isemid.features.patient.models.Affiliation;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Form0581MapperHelper {

    @Named("mapAffiliation")
    public Boolean mapAffiliation(Form0581 form0581, @Context Long orgId) {
        List<Affiliation> affiliations = form0581.getPatient().getAffiliations();
        if (affiliations == null || affiliations.isEmpty() || orgId == null) {
            return false;
        }
        return affiliations.stream()
                .anyMatch(a -> orgId.equals(a.getOrganizationId()));
    }
}

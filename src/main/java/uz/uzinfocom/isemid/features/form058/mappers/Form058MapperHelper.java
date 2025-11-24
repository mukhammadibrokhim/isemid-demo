package uz.uzinfocom.isemid.features.form058.mappers;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Context;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.patient.models.Affiliation;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Form058MapperHelper {

    @Named("mapAffiliation")
    public Boolean mapAffiliation(Form058 form058, @Context Long orgId) {
        List<Affiliation> affiliations = form058.getPatient().getAffiliations();
        if (affiliations == null || affiliations.isEmpty() || orgId == null) {
            return false;
        }
        return affiliations.stream()
                .anyMatch(a -> orgId.equals(a.getOrganizationId()));
    }
    @Named("toUpperCase")
    public String toUpperCase(String value) {
        return value != null ? value.trim().toUpperCase() : null;
    }

}

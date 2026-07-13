package uz.uzinfocom.isemid.features.form0581.mappers;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.form058.models.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;
import uz.uzinfocom.isemid.features.form0581.dtos.responses.Form0581DetailsResponse;
import uz.uzinfocom.isemid.features.form0581.dtos.responses.Form0581Response;
import uz.uzinfocom.isemid.features.form0581.models.Form0581;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.Objects;

@Component
public class Form0581ResponseAdapter {

    public Form0581Response adapt(Form0581Response response, Form0581 form, FilterType filterType) {
        if (filterType == FilterType.RECEIVER && form.getStatus() == FormStatus.SENT) {
            response.setStatus(FormStatus.NEW);
        }
        return response;
    }

    public Form0581DetailsResponse adapt(Form0581DetailsResponse response, Form0581 form, FilterType filterType) {
        if (filterType == FilterType.RECEIVER && form.getStatus() == FormStatus.SENT) {
            response.setStatus(FormStatus.NEW);
        }

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        boolean hasAffiliation = form.getPatient() != null &&
                form.getPatient().getAffiliations().stream()
                        .anyMatch(a -> Objects.equals(a.getOrganizationId(), currentUser.getCurrentOrganization().getId()));

        response.setAffiliation(hasAffiliation);

        return response;
    }
}

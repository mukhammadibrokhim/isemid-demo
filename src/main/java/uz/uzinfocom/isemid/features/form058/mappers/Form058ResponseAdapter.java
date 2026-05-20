package uz.uzinfocom.isemid.features.form058.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.card.dtos.responses.CardSimpleResponse;
import uz.uzinfocom.isemid.features.card.mappers.CardMapper;
import uz.uzinfocom.isemid.features.form058.dtos.responses.Form058DetailsResponse;
import uz.uzinfocom.isemid.features.form058.dtos.responses.Form058Response;
import uz.uzinfocom.isemid.features.form058.models.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Form058ResponseAdapter {
    private final CardMapper cardMapper;

    public Form058Response adapt(Form058Response response, Form058 form, FilterType filterType) {
        if (filterType == FilterType.RECEIVER && form.getStatus() == FormStatus.SENT) {
            response.setStatus(FormStatus.NEW);
        }
        return response;
    }

    public Form058DetailsResponse adapt(Form058DetailsResponse response, Form058 form, FilterType filterType) {
        if (filterType == FilterType.RECEIVER && form.getStatus() == FormStatus.SENT) {
            response.setStatus(FormStatus.NEW);
        }

        if (form.getCards() != null && !form.getCards().isEmpty()) {
            List<CardSimpleResponse> simpleCards = form.getCards().stream()
                    .map(cardMapper::toSimpleResponse)
                    .collect(Collectors.toList());
            response.setCards(simpleCards);
        }
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        boolean hasAffiliation = form.getPatient() != null &&
                form.getPatient().getAffiliations().stream()
                        .anyMatch(a -> Objects.equals(a.getOrganizationId(), currentUser.getCurrentOrganization().getId()));

        response.setAffiliation(hasAffiliation);

        return response;
    }
}

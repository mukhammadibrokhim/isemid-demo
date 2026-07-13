package uz.uzinfocom.isemid.features.form0581.policy;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;
import uz.uzinfocom.isemid.features.form0581.models.Form0581;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Component
@RequiredArgsConstructor
public class Form0581Policy {
    private final MessageUtil messageUtil;

    private static final Set<CardType> ASSIGNABLE_CARD_TYPES = Set.of(
            CardType.CARD174,
            CardType.CARD175,
            CardType.CARD205
    );

    private static final EnumSet<FormStatus> EDIT_FORBIDDEN = EnumSet.of(
            FormStatus.CARD_LINKED,
            FormStatus.CARD_REJECTED,
            FormStatus.APPROVED_PENDING,
            FormStatus.APPROVED,
            FormStatus.CANCELED
    );

    public void assertEditable(Form0581 form) {
        if (EDIT_FORBIDDEN.contains(form.getStatus())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    messageUtil.get("error.form0581.edit_forbidden_by_status", form.getStatus())
            );
        }
    }

    public void assertCardAssignable(Form0581 form0581, List<CardType> requestedCardTypes) {
        boolean hasDisallowedType = requestedCardTypes.stream()
                .anyMatch(type -> !ASSIGNABLE_CARD_TYPES.contains(type));

        if (hasDisallowedType) {
            throw new ApiException(
                    BAD_REQUEST,
                    messageUtil.get("error.form0581.card_type_not_allowed")
            );
        }
    }

    private static final EnumSet<FormStatus> CANCEL_FORBIDDEN = EnumSet.of(
            FormStatus.APPROVED_PENDING,
            FormStatus.APPROVED,
            FormStatus.CANCELED,
            FormStatus.CARD_REJECTED
    );

    public void assertCancelable(Form0581 form0581) {
        if (CANCEL_FORBIDDEN.contains(form0581.getStatus())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    messageUtil.get("error.form0581.cancel_forbidden_by_status", form0581.getStatus())
            );
        }
    }

    private static final EnumSet<FormStatus> APPROVABLE_STATUSES = EnumSet.of(
            FormStatus.CARD_LINKED,
            FormStatus.APPROVED_PENDING
    );

    public void assertApprovable(Form0581 form0581) {
        if (form0581 == null) {
            throw new IllegalArgumentException("Form0581 must not be null");
        }

        if (!APPROVABLE_STATUSES.contains(form0581.getStatus())) {
            throw new ApiException(BAD_REQUEST,
                    "Form0581 can be approved only when status is CARD_LINKED or APPROVED_PENDING. Current status: "
                            + form0581.getStatus()
            );
        }
    }

    public void assertDeletable(Form0581 form0581) {
        if (form0581.getStatus() != FormStatus.SENT) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    messageUtil.get("error.form0581.delete_forbidden_by_status", form0581.getStatus())
            );
        }
    }

    public void assertNotApprovable(Form0581 form0581) {
        //TODO Should add status validation
    }

    public void assertDiagnosisApprovable(Form0581 form0581) {
        if (form0581 == null) {
            throw new ApiException(FORBIDDEN, "error.form0581.not_found");
        }

        if (!APPROVABLE_STATUSES.contains(form0581.getStatus())) {
            throw new ApiException(
                    FORBIDDEN,
                    "error.form0581.diagnosis_approval_not_allowed_status"
            );
        }
    }

    public void validateCreatPermission(Organization currentOrganization) {
        if (currentOrganization == null) {
            throw new ApiException(FORBIDDEN, "error.organization.current.not_found");
        }

        boolean isRepublicanOrRegional =
                LevelType.REPUBLICAN == currentOrganization.getLevelType()
                        || LevelType.REGIONAL == currentOrganization.getLevelType();

        boolean isSanepidService =
                MedicalType.SANEPID_SERVICE == currentOrganization.getMedicalType();

        if (isRepublicanOrRegional && isSanepidService) {
            throw new ApiException(FORBIDDEN, "error.create.not_allowed_for_republican_or_regional_sanepid");
        }
    }
}

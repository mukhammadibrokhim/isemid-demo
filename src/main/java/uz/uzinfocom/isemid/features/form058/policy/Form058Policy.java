package uz.uzinfocom.isemid.features.form058.policy;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;

import java.util.EnumSet;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Component
@RequiredArgsConstructor
public class Form058Policy {
    private final MessageUtil messageUtil;

    private static final EnumSet<FormStatus> EDIT_FORBIDDEN = EnumSet.of(
            FormStatus.CARD_LINKED,
            FormStatus.CARD_REJECTED,
            FormStatus.APPROVED_PENDING,
            FormStatus.APPROVED,
            FormStatus.CANCELED
    );

    public void assertEditable(Form058 form) {
        if (EDIT_FORBIDDEN.contains(form.getStatus())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    messageUtil.get("error.form058.edit_forbidden_by_status", form.getStatus())
            );
        }
    }

    public void assertCardAssignable(Form058 form058) {
        //TODO Should add status validation
    }

    public void assertCancelable(Form058 form058) {
        //TODO Should add status validation
    }

    public void assertApprovable(Form058 form058) {
        if (form058 == null) {
            throw new IllegalArgumentException("Form058 must not be null");
        }

        if (form058.getStatus() != FormStatus.CARD_LINKED) {
            throw new ApiException(BAD_REQUEST,
                    "Form058 can be approved only when status is CARD_LINKED. Current status: "
                            + form058.getStatus()
            );
        }
    }
    public void assertNotApprovable(Form058 form058) {
        //TODO Should add status validation
    }

    public void assertDeletable(Form058 form058) {
        //TODO Should add status validation
    }

    public void assertDiagnosisApprovable(Form058 form058) {
        if (form058 == null) {
            throw new ApiException(FORBIDDEN, "error.form058.not_found");
        }

        if (form058.getStatus() != FormStatus.CARD_LINKED) {
            throw new ApiException(
                    FORBIDDEN,
                    "error.form058.diagnosis_approval_not_allowed_status"
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
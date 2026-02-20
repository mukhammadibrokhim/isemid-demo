package uz.uzinfocom.isemid.features.form058.policy;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;

import java.util.EnumSet;

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
        //TODO Should add status validation
    }

    public void assertNotApprovable(Form058 form058) {
        //TODO Should add status validation
    }

    public void assertDeletable(Form058 form058) {
        //TODO Should add status validation
    }

    public void assertDiagnosisApprovable(Form058 form058) {
        //TODO Should add status validation
    }
}
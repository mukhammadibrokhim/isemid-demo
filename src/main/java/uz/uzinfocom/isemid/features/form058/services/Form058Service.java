package uz.uzinfocom.isemid.features.form058.services;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.form058.dtos.requests.*;
import uz.uzinfocom.isemid.features.form058.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.Map;

public interface Form058Service {
    Form058 create(Form058Request form058Request);

    Page<Form058> filter(
            int page,
            int size,
            LocalDate fromDate,
            LocalDate toDate,
            String stateCode,
            String cityCode,
            Boolean isAffiliation, User user,
            FilterType filterType
    );

    Form058 update(Form058Request request, Long id, User user);

    Form058 getById(long id);

    Form058 assignCard(Form058AssignCardRequest request, User user);

    Form058 changeReceivedOrd(Long formId, Long orgId);

    Form058 cancel(Long formId);

    Form058 approveForm(Long formId);

    Form058 notApprove(Long formId, Form058NotApproveRequest notApprove);

    void deleteById(Long formId);

    Form058 approveDiagnosis(Long formId, Form058ChangeDiagnosisRequest changeDiagnosis);

    Map<String, Long> getConfirmationStats();

}

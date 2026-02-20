package uz.uzinfocom.isemid.features.form058.services;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.form058.dtos.requests.Form058ApproveDiagnosisRequest;
import uz.uzinfocom.isemid.features.form058.dtos.requests.Form058AssignCardRequest;
import uz.uzinfocom.isemid.features.form058.dtos.requests.Form058NotApproveRequest;
import uz.uzinfocom.isemid.features.form058.dtos.requests.Form058Request;
import uz.uzinfocom.isemid.features.form058.dtos.responses.Form058ConfirmationStatsResponse;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.models.enums.FilterType;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.List;

public interface Form058Service {
    Form058 create(Form058Request form058Request, String sourceHeader, User currentUser);

    Page<Form058> filter(
            int page,
            int size,
            LocalDate fromDate,
            LocalDate toDate,
            String stateCode,
            String cityCode,
            Boolean isAffiliation,
            String mkb10Code,
            Long organizationId,
            String patientDocValue, User user,
            FilterType filterType
    );

    Page<Form058> getByPatientNnuzb(int page, int size, String nnuzb);

    Form058 update(Form058Request request, Long id, User user);

    Form058 getById(long id);

    Form058 assignCard(Form058AssignCardRequest request, User user);

    Form058 changeReceivedOrd(Long formId, Long orgId);

    Form058 cancel(Long formId, String reason, User currentUser);

    Form058 approveForm(Long formId, User user);

    Form058 notApprove(Long formId, User user, Form058NotApproveRequest notApprove);

    void deleteById(Long formId);

    Form058 approveDiagnosis(Long formId, Form058ApproveDiagnosisRequest changeDiagnosis, String token,
                             User currentUser);

    List<Form058ConfirmationStatsResponse> getConfirmationStats(List<String> mkb10Codes, List<String> regionCodes,
                                                                LocalDate fromDate, LocalDate toDate,
                                                                String locale, User currentUser);

    Form058 getByCardId(Long cardId);

}

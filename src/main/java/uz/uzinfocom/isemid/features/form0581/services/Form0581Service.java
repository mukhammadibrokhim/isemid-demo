package uz.uzinfocom.isemid.features.form0581.services;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.form058.dtos.responses.Form058ConfirmationStatsResponse;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.Form0581ApproveDiagnosisRequest;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.Form0581AssignCardRequest;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.Form0581FilterRequest;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.Form0581NotApproveRequest;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.Form0581Request;
import uz.uzinfocom.isemid.features.form0581.models.Form0581;
import uz.uzinfocom.isemid.features.form058.models.enums.FilterType;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.List;

public interface Form0581Service {
    Form0581 create(Form0581Request request, String sourceHeader, User currentUser);

    Page<Form0581> getAll(Form0581FilterRequest filterRequest, User user, FilterType filterType);

    Page<Form0581> getByPatientNnuzb(int page, int size, String nnuzb);

    Form0581 update(Form0581Request request, Long id, User user);

    Form0581 getById(long id);

    Form0581 assignCard(Form0581AssignCardRequest request, User user);

    Form0581 changeReceivedOrg(Long formId, Long orgId);

    Form0581 cancel(Long formId, String reason, User currentUser);

    Form0581 approveForm(Long formId, User user);

    Form0581 notApprove(Long formId, User user, Form0581NotApproveRequest notApprove);

    Form0581 approveDiagnosis(Long formId, Form0581ApproveDiagnosisRequest changeDiagnosis, String token, User currentUser);

    void deleteById(Long formId);

    Form0581 getByCardId(Long cardId);

    List<Form058ConfirmationStatsResponse> getConfirmationStats(List<String> mkb10Codes, List<String> regionCodes,
                                                                 LocalDate fromDate, LocalDate toDate,
                                                                 String locale, User currentUser);
}

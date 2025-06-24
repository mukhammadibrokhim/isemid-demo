package uz.uzinfocom.isemid.features.form058.services;


import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.features.form058.dtos.requests.*;
import uz.uzinfocom.isemid.features.form058.dtos.responses.Form058Response;
import uz.uzinfocom.isemid.features.form058.enums.FilterType;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface Form058Service {
    ApiResponse<Form058Response> create(Form058Request form058Request, User user,String token);

    ApiResponse<Form058Response> update(Form058Request request, Long id, User user);

    PagedResponse<?> getAllFilteredForm058(int page, int size, LocalDate fromDate, LocalDate toDate, User user, String url, FilterType filterType);

    ApiResponse<Form058Response> getById(long id);

    ApiResponse<Form058Response> assignCard(Form058AssignCardRequest request, User user);

    ApiResponse<Form058Response> changeReceivedOrd(Long formId, Form058UpdateReceivedOrg updateReceivedOrg);

    ApiResponse<Form058Response> cancel(Long formId);

    ApiResponse<Form058Response> approveForm(Long formId);

    ApiResponse<Form058Response> notApprove(Long formId, Form058NotApproveRequest notApprove);

    ApiResponse<?> deleteById(Long formId);

    ApiResponse<?> changeDiagnosis(Long formId, Form058ChangeDiagnosisRequest changeDiagnosis);
}

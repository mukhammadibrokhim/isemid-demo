package uz.uzinfocom.isemid.features.report.service;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.report.dtos.request.FormReport7Request;
import uz.uzinfocom.isemid.features.report.model.FormReport7;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface FormReport7Service {

    Page<FormReport7> getReport(
            int page, int size, LocalDate fromDate, LocalDate toDate, String stateCode,
            String cityCode, User currentUser
    );

    FormReport7 create(@Valid FormReport7Request request, User currentUser);

    FormReport7 getById(Long id);

    FormReport7 update(Long id, FormReport7Request request);

    void delete(Long id);

}

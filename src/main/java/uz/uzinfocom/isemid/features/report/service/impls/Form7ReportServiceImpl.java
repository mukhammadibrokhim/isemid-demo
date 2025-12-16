package uz.uzinfocom.isemid.features.report.service.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.commons.utils.PageableUtils;
import uz.uzinfocom.isemid.features.report.dtos.request.Form7ReportRequest;
import uz.uzinfocom.isemid.features.report.mapper.Form7ReportMapper;
import uz.uzinfocom.isemid.features.report.model.FormReport7;
import uz.uzinfocom.isemid.features.report.repository.FormReport7Repository;
import uz.uzinfocom.isemid.features.report.service.Form7ReportService;
import uz.uzinfocom.isemid.features.report.specification.FormReport7Specification;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class Form7ReportServiceImpl implements Form7ReportService {
    private final FormReport7Repository reportRepository;
    private final Form7ReportMapper form7ReportMapper;
    private final MessageUtil messageUtil;

    @Override
    public Page<FormReport7> getReport(
            int page,
            int size,
            LocalDate fromDate,
            LocalDate toDate,
            String stateCode,
            String cityCode,
            User currentUser
    ) {
        Specification<FormReport7> spec = FormReport7Specification.build(fromDate, toDate, stateCode, cityCode, currentUser);
        return reportRepository.findAll(spec, PageableUtils.pageableSortByIdDesc(page, size));
    }

    @Override
    public FormReport7 create(User currentUser) {
        return null;
    }

    @Override
    public FormReport7 getById(Long id) {
        return reportRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Override
    public FormReport7 update(Long id, Form7ReportRequest request) {
        FormReport7 formReport7 = getById(id);
        form7ReportMapper.update(formReport7, request);
        return reportRepository.save(formReport7);
    }

    @Override
    public void delete(Long id) {
        FormReport7 report = getById(id);
        try {
            reportRepository.delete(report);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Report cannot be deleted because it is referenced by other data."
            );
        } catch (Exception e) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    messageUtil.get("error.internal_server")
            );
        }
    }
}
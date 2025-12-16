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
import uz.uzinfocom.isemid.features.report.dtos.request.Form3_2ReportRequest;
import uz.uzinfocom.isemid.features.report.mapper.FormReport32Mapper;
import uz.uzinfocom.isemid.features.report.model.FormReport32;
import uz.uzinfocom.isemid.features.report.repository.FormReport32ReportRepository;
import uz.uzinfocom.isemid.features.report.service.FormReport32Service;
import uz.uzinfocom.isemid.features.report.specification.FormReport32Specification;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class FormReport32ServiceImpl implements FormReport32Service {
    private final FormReport32ReportRepository reportRepository;
    private final FormReport32Mapper mapper;
    private final MessageUtil messageUtil;

    @Override
    public Page<FormReport32> getReport(int page, int size, LocalDate fromDate, LocalDate toDate, String stateCode,
                                        String cityCode, User currentUser) {
        Specification<FormReport32> spec = FormReport32Specification.build(fromDate, toDate, stateCode, cityCode, currentUser);
        return reportRepository.findAll(spec, PageableUtils.pageableSortByIdDesc(page, size));
    }

    @Override
    public FormReport32 create(Form3_2ReportRequest request) {
        FormReport32 entity = mapper.entity(request);
        return reportRepository.save(entity);
    }

    @Override
    public FormReport32 getById(Long id) {
        return reportRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Override
    public FormReport32 update(Long id, Form3_2ReportRequest request) {
        FormReport32 entity = getById(id);
        mapper.update(entity, request);
        return reportRepository.save(entity);
    }

    @Override
    public void delete(Long id) {
        FormReport32 report = getById(id);
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

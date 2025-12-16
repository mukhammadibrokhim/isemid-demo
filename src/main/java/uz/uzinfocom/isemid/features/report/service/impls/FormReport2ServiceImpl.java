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
import uz.uzinfocom.isemid.features.report.dtos.request.Form2ReportRequest;
import uz.uzinfocom.isemid.features.report.mapper.FormReport2Mapper;
import uz.uzinfocom.isemid.features.report.model.FormReport2;
import uz.uzinfocom.isemid.features.report.repository.FormReport2Repository;
import uz.uzinfocom.isemid.features.report.service.FormReport2Service;
import uz.uzinfocom.isemid.features.report.specification.FormReport2Specification;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class FormReport2ServiceImpl implements FormReport2Service {
    private final FormReport2Repository reportRepository;
    private final FormReport2Mapper mapper;
    private final MessageUtil messageUtil;

    @Override
    public Page<FormReport2> getReport(
            int page, int size, LocalDate fromDate, LocalDate toDate, String stateCode,
            String cityCode, User currentUser
    ) {

        Specification<FormReport2> spec = FormReport2Specification.build(fromDate, toDate, stateCode, cityCode, currentUser);
        return reportRepository.findAll(spec, PageableUtils.pageableSortByIdDesc(page, size));
    }

    @Override
    public FormReport2 create(Form2ReportRequest request) {
        FormReport2 form2Report = mapper.toEntity(request);
        return reportRepository.save(form2Report);
    }

    @Override
    public FormReport2 getById(Long id) {
        return reportRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Override
    public FormReport2 update(Long id, Form2ReportRequest request) {
        FormReport2 entity = getById(id);
        mapper.update(entity, request);
        return reportRepository.save(entity);
    }

    @Override
    public void delete(Long id) {
        FormReport2 report = getById(id);
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

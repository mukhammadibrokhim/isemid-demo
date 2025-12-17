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
import uz.uzinfocom.isemid.features.report.dtos.request.Form3_1ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.request.Form7ReportRequest;
import uz.uzinfocom.isemid.features.report.mapper.FormReport31Mapper;
import uz.uzinfocom.isemid.features.report.model.FormReport31;
import uz.uzinfocom.isemid.features.report.repository.FormReport31ReportRepository;
import uz.uzinfocom.isemid.features.report.service.FormReport31Service;
import uz.uzinfocom.isemid.features.report.specification.FormReport31Specification;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class FormReport31ServiceImpl implements FormReport31Service {
    private final FormReport31ReportRepository repository;
    private final FormReport31Mapper mapper;
    private final MessageUtil messageUtil;

    @Override
    public Page<FormReport31> getReport(
            int page, int size, LocalDate fromDate, LocalDate toDate, String stateCode,
            String cityCode, User currentUser) {
        Specification<FormReport31> spec = FormReport31Specification.build(fromDate, toDate, stateCode, cityCode, currentUser);
        return repository.findAll(spec, PageableUtils.pageableSortByIdDesc(page, size));
    }

    @Override
    public FormReport31 create(Form3_1ReportRequest request) {
        FormReport31 entity = mapper.toEntity(request);
        return repository.save(entity);
    }

    @Override
    public FormReport31 getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Override
    public FormReport31 update(Long id, Form7ReportRequest request) {
        FormReport31 entity = getById(id);
        mapper.update(entity, request);
        return repository.save(entity);
    }

    @Override
    public void delete(Long id) {
        FormReport31 report = getById(id);
        try {
            repository.delete(report);
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

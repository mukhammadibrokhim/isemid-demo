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
import uz.uzinfocom.isemid.features.report.mapper.Form3_1ReportMapper;
import uz.uzinfocom.isemid.features.report.model.Form3_1Report;
import uz.uzinfocom.isemid.features.report.repository.Form3_1ReportRepository;
import uz.uzinfocom.isemid.features.report.service.Form3_1ReportService;
import uz.uzinfocom.isemid.features.report.specification.Form3_1ReportSpecification;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class form3_1ReportServiceImpl implements Form3_1ReportService {
    private final Form3_1ReportRepository repository;
    private final Form3_1ReportMapper mapper;
    private final MessageUtil messageUtil;

    @Override
    public Page<Form3_1Report> getReport(
            int page, int size, LocalDate fromDate, LocalDate toDate, String stateCode,
            String cityCode, User currentUser) {
        Specification<Form3_1Report> spec = Form3_1ReportSpecification.build(fromDate, toDate, stateCode, cityCode, currentUser);
        return repository.findAll(spec, PageableUtils.pageableSortByIdDesc(page, size));
    }

    @Override
    public Form3_1Report create(Form3_1ReportRequest request) {
        Form3_1Report entity = mapper.toEntity(request);
        return repository.save(entity);
    }

    @Override
    public Form3_1Report getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Override
    public Form3_1Report update(Long id, Form7ReportRequest request) {
        Form3_1Report entity = getById(id);
        mapper.update(entity, request);
        return repository.save(entity);
    }

    @Override
    public void delete(Long id) {
        Form3_1Report report = getById(id);
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

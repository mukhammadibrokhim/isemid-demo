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
import uz.uzinfocom.isemid.features.report.model.Form7Report;
import uz.uzinfocom.isemid.features.report.repository.Form7ReportRepository;
import uz.uzinfocom.isemid.features.report.service.Form7ReportService;
import uz.uzinfocom.isemid.features.report.specification.Form7ReportSpecification;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class Form7ReportServiceImpl implements Form7ReportService {
    private final Form7ReportRepository reportRepository;
    private final Form7ReportMapper form7ReportMapper;
    private final MessageUtil messageUtil;

    @Override
    public Page<Form7Report> getReport(
            int page,
            int size,
            LocalDate fromDate,
            LocalDate toDate,
            String stateCode,
            String cityCode,
            User currentUser
    ) {
        Specification<Form7Report> spec = Form7ReportSpecification.build(fromDate, toDate, stateCode, cityCode, currentUser);
        return reportRepository.findAll(spec, PageableUtils.pageableSortByIdDesc(page, size));
    }

    @Override
    public Form7Report create(Form7ReportRequest request) {
        Form7Report form7Report = form7ReportMapper.toEntity(request);
        return reportRepository.save(form7Report);
    }

    @Override
    public Form7Report getById(Long id) {
        return reportRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Override
    public Form7Report update(Long id, Form7ReportRequest request) {
        Form7Report form7Report = getById(id);
        form7ReportMapper.update(form7Report, request);
        return reportRepository.save(form7Report);
    }

    @Override
    public void delete(Long id) {
        Form7Report report = getById(id);
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
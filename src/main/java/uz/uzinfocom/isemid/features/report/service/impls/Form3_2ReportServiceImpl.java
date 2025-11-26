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
import uz.uzinfocom.isemid.features.report.mapper.Form3_2ReportMapper;
import uz.uzinfocom.isemid.features.report.model.Form3_2Report;
import uz.uzinfocom.isemid.features.report.repository.Form3_2ReportRepository;
import uz.uzinfocom.isemid.features.report.service.Form3_2ReportService;
import uz.uzinfocom.isemid.features.report.specification.Form3_2ReportSpecification;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class Form3_2ReportServiceImpl implements Form3_2ReportService {
    private final Form3_2ReportRepository reportRepository;
    private final Form3_2ReportMapper mapper;
    private final MessageUtil messageUtil;

    @Override
    public Page<Form3_2Report> getReport(int page, int size, LocalDate fromDate, LocalDate toDate, String stateCode,
                                         String cityCode, User currentUser) {
        Specification<Form3_2Report> spec = Form3_2ReportSpecification.build(fromDate, toDate, stateCode, cityCode, currentUser);
        return reportRepository.findAll(spec, PageableUtils.pageableSortByIdDesc(page, size));
    }

    @Override
    public Form3_2Report create(Form3_2ReportRequest request) {
        Form3_2Report entity = mapper.entity(request);
        return reportRepository.save(entity);
    }

    @Override
    public Form3_2Report getById(Long id) {
        return reportRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Override
    public Form3_2Report update(Long id, Form3_2ReportRequest request) {
        Form3_2Report entity = getById(id);
        mapper.update(entity, request);
        return reportRepository.save(entity);
    }

    @Override
    public void delete(Long id) {
        Form3_2Report report = getById(id);
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

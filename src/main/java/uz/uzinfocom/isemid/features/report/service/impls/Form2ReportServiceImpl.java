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
import uz.uzinfocom.isemid.features.report.mapper.Form2ReportMapper;
import uz.uzinfocom.isemid.features.report.model.Form2Report;
import uz.uzinfocom.isemid.features.report.repository.Form2ReportRepository;
import uz.uzinfocom.isemid.features.report.service.Form2ReportService;
import uz.uzinfocom.isemid.features.report.specification.Form2ReportSpecification;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class Form2ReportServiceImpl implements Form2ReportService {
    private final Form2ReportRepository reportRepository;
    private final Form2ReportMapper mapper;
    private final MessageUtil messageUtil;

    @Override
    public Page<Form2Report> getReport(
            int page, int size, LocalDate fromDate, LocalDate toDate, String stateCode,
            String cityCode, User currentUser
    ) {

        Specification<Form2Report> spec = Form2ReportSpecification.build(fromDate, toDate, stateCode, cityCode, currentUser);
        return reportRepository.findAll(spec, PageableUtils.pageableSortByIdDesc(page, size));
    }

    @Override
    public Form2Report create(Form2ReportRequest request) {
        Form2Report form2Report = mapper.toEntity(request);
        return reportRepository.save(form2Report);
    }

    @Override
    public Form2Report getById(Long id) {
        return reportRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Override
    public Form2Report update(Long id, Form2ReportRequest request) {
        Form2Report entity = getById(id);
        mapper.update(entity, request);
        return reportRepository.save(entity);
    }

    @Override
    public void delete(Long id) {
        Form2Report report = getById(id);
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

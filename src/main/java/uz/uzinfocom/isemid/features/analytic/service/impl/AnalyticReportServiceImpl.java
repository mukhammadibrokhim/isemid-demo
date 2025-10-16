package uz.uzinfocom.isemid.features.analytic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.commons.utils.PageableUtils;
import uz.uzinfocom.isemid.features.analytic.dto.request.AnalyticReportRequest;
import uz.uzinfocom.isemid.features.analytic.mapper.AnalyticReportMapper;
import uz.uzinfocom.isemid.features.analytic.model.AnalyticReport;
import uz.uzinfocom.isemid.features.analytic.repository.AnalyticReportRepository;
import uz.uzinfocom.isemid.features.analytic.service.AnalyticReportService;
import uz.uzinfocom.isemid.features.analytic.specifications.AnalyticReportSpecification;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AnalyticReportServiceImpl implements AnalyticReportService {
    private final AnalyticReportRepository aRepository;
    private final AnalyticReportMapper aMapper;
    private final MessageUtil messageUtil;

    @Override
    public Page<AnalyticReport> getAnalyticReport(
            int page, int size, LocalDate fromDate, LocalDate toDate,
            String regionCode, String mkb10Code, User user
    ) {
        Specification<AnalyticReport> spec = AnalyticReportSpecification.build(fromDate, toDate, regionCode, mkb10Code, user);
        return aRepository.findAll(spec, PageableUtils.pageableSortByIdDesc(page, size));
    }

    @Override
    public AnalyticReport create(AnalyticReportRequest request) {
        return aRepository.save(aMapper.toEntity(request));
    }

    @Override
    public AnalyticReport getOne(Long id) {
        return aRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Override
    public AnalyticReport update(Long id, AnalyticReportRequest request) {
        AnalyticReport report = getOne(id);
        aMapper.update(report, request);
        return aRepository.save(report);
    }

    @Override
    public void delete(Long id) {
        AnalyticReport report = getOne(id);
        try {
            aRepository.delete(report);
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

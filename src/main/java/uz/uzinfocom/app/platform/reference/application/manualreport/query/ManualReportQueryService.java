package uz.uzinfocom.app.platform.reference.application.manualreport.query;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.platform.reference.application.manualreport.query.dto.ManualReportFilterRequest;
import uz.uzinfocom.app.platform.reference.application.manualreport.query.dto.ManualReportResponse;
import uz.uzinfocom.app.platform.reference.application.manualreport.query.dto.ManualReportTableResponse;
import uz.uzinfocom.app.platform.reference.application.manualreport.query.mapper.ManualReportMapper;
import uz.uzinfocom.app.platform.reference.application.manualreport.query.projection.ManualReportTableProjection;
import uz.uzinfocom.app.platform.reference.application.manualreport.query.specification.ManualReportSpecification;
import uz.uzinfocom.app.platform.reference.config.ReferenceCacheConfig;
import uz.uzinfocom.app.platform.reference.repository.ManualReportRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class ManualReportQueryService {

    private final ManualReportRepository manualReportRepository;
    private final ManualReportMapper manualReportMapper;

    @Transactional(readOnly = true)
    public Page<ManualReportTableResponse> findTable(ManualReportFilterRequest request) {
        ManualReportFilterRequest filter = request == null
                ? new ManualReportFilterRequest(null, null, null, null, null, null)
                : request;
        Pageable pageable = PageableUtils.of(filter, ManualReportSortFields.ALLOWED_SORT_FIELDS);

        Page<ManualReportTableProjection> page = Objects.requireNonNull(manualReportRepository.findBy(
                ManualReportSpecification.byFilter(filter),
                query -> query
                        .as(ManualReportTableProjection.class)
                        .page(pageable)
                ), "Manual Report Table page is returned null"
        );

        return page.map(manualReportMapper::toTableResponse);
    }

    @Transactional(readOnly = true)
    public ManualReportResponse getById(Long id) {
        return manualReportRepository.findByIdAndDeletedFalse(id)
                .map(manualReportMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("reference.manual_report.not_found_by_id", id));
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = ReferenceCacheConfig.REF_MANUAL_REPORT_BY_CODE,
            key = "#code.trim().toUpperCase(T(java.util.Locale).ROOT)",
            condition = "#code != null"
    )
    public ManualReportResponse getByCode(String code) {
        String normalizedCode = ReferenceCodeNormalizer.normalizeCode(code);

        return manualReportRepository.findByCodeAndDeletedFalse(normalizedCode)
                .map(manualReportMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("reference.manual_report.not_found_by_code", normalizedCode));
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = ReferenceCacheConfig.REF_MANUAL_REPORTS_BY_MKB10_CODE,
            key = "#mkb10Code.trim().toUpperCase(T(java.util.Locale).ROOT)",
            condition = "#mkb10Code != null"
    )
    public List<ManualReportResponse> getByMkb10Code(String mkb10Code) {
        String normalizedCode = mkb10Code == null ? null : mkb10Code.trim().toUpperCase(Locale.ROOT);

        return manualReportRepository.findAllByMkb10CodeAndDeletedFalse(normalizedCode)
                .stream()
                .map(manualReportMapper::toResponse)
                .toList();
    }
}

package uz.uzinfocom.app.platform.reference.application.country.query;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.platform.reference.application.country.query.dto.CountryFilterRequest;
import uz.uzinfocom.app.platform.reference.application.country.query.dto.CountryDetailedResponse;
import uz.uzinfocom.app.platform.reference.application.country.query.dto.CountryTableResponse;
import uz.uzinfocom.app.platform.reference.application.country.query.mapper.CountryMapper;
import uz.uzinfocom.app.platform.reference.application.country.query.projection.CountryTableProjection;
import uz.uzinfocom.app.platform.reference.application.country.query.specification.CountrySpecification;
import uz.uzinfocom.app.platform.reference.config.ReferenceCacheConfig;
import uz.uzinfocom.app.platform.reference.repository.CountryRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;
import java.util.Objects;

@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class CountryQueryService {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    @Transactional(readOnly = true)
    public Page<CountryTableResponse> findTable(CountryFilterRequest request) {
        CountryFilterRequest filter = request == null
                ? new CountryFilterRequest(null, null, null, null, null, null)
                : request;
        Pageable pageable = PageableUtils.of(filter, CountrySortFields.ALLOWED_SORT_FIELDS);

        Page<CountryTableProjection> page = Objects.requireNonNull(countryRepository.findBy(
                CountrySpecification.byFilter(filter),
                query -> query
                        .as(CountryTableProjection.class)
                        .page(pageable)
                ), "Country Table page is returned null"
        );

        return page.map(countryMapper::toTableResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ReferenceCacheConfig.REF_COUNTRIES, key = "'all'")
    public List<CountryDetailedResponse> getAll() {
        return countryRepository.findAllByDeletedFalseOrderByNameUzAsc()
                .stream()
                .map(countryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CountryDetailedResponse getById(Long id) {
        return countryRepository.findByIdAndDeletedFalse(id)
                .map(countryMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("reference.country.not_found_by_id", id));
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = ReferenceCacheConfig.REF_COUNTRY_BY_CODE,
            key = "#code.trim().toUpperCase(T(java.util.Locale).ROOT)",
            condition = "#code != null"
    )
    public CountryDetailedResponse getByCode(String code) {
        String normalizedCode = ReferenceCodeNormalizer.normalizeCode(code);

        return countryRepository.findByCodeAndDeletedFalse(normalizedCode)
                .map(countryMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("reference.country.not_found_by_code", normalizedCode));
    }
}

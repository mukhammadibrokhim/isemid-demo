package uz.uzinfocom.app.modules.reference.application.catalog.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.reference.application.catalog.query.dto.CatalogFilterRequest;
import uz.uzinfocom.app.modules.reference.application.catalog.query.dto.CatalogLookupFilterRequest;
import uz.uzinfocom.app.modules.reference.application.catalog.query.dto.CatalogLookupResponse;
import uz.uzinfocom.app.modules.reference.application.catalog.query.dto.CatalogResponse;
import uz.uzinfocom.app.modules.reference.application.catalog.query.dto.CatalogTableResponse;
import uz.uzinfocom.app.modules.reference.application.catalog.query.mapper.CatalogMapper;
import uz.uzinfocom.app.modules.reference.application.catalog.query.projection.CatalogTableProjection;
import uz.uzinfocom.app.modules.reference.application.catalog.query.specification.CatalogSpecification;
import uz.uzinfocom.app.modules.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.modules.reference.domain.Catalog;
import uz.uzinfocom.app.modules.reference.repository.CatalogRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CatalogQueryService {

    private static final int DEFAULT_LOOKUP_LIMIT = 20;
    private static final int MAX_LOOKUP_LIMIT = 200;

    private final CatalogRepository catalogRepository;
    private final CatalogMapper catalogMapper;

    @Transactional(readOnly = true)
    public Page<CatalogTableResponse> findTable(CatalogFilterRequest request) {
        Pageable pageable = PageableUtils.of(request, CatalogSortFields.ALLOWED_SORT_FIELDS);

        Page<CatalogTableProjection> page = Objects.requireNonNull(
                catalogRepository.findBy(
                        CatalogSpecification.byFilter(request),
                        query -> query
                                .as(CatalogTableProjection.class)
                                .page(pageable)
                ),
                "Catalog page return null!"
        );

        return page.map(catalogMapper::toTableResponse);
    }

    @Transactional(readOnly = true)
    public CatalogResponse getById(Long id) {
        return catalogRepository.findByIdAndDeletedFalse(id)
                .map(catalogMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("reference.catalog.not_found_by_id", id));
    }

    @Transactional(readOnly = true)
    public CatalogLookupResponse getByTypeAndCode(String  type, String code) {
        String normalizedCode = ReferenceCodeNormalizer.normalizeCode(code);

        return catalogRepository.findByTypeAndCodeAndDeletedFalse(type, normalizedCode)
                .map(catalogMapper::toLookupResponse)
                .orElseThrow(() ->
                        new NotFoundException("reference.catalog.not_found_by_type_code", type, normalizedCode));
    }

    @Transactional(readOnly = true)
    public List<CatalogLookupResponse> getByType(String type, CatalogLookupFilterRequest request) {
        Pageable pageable = PageableUtils.limitOnly(
                request.limit(),
                "nameUz",
                Sort.Direction.ASC,
                DEFAULT_LOOKUP_LIMIT,
                MAX_LOOKUP_LIMIT
        );

        return catalogRepository.findAll(
                        CatalogSpecification.byTypeAndName(type, request.name()),
                        pageable
                )
                .map(catalogMapper::toLookupResponse)
                .getContent();
    }

    @Transactional(readOnly = true)
    public List<CatalogLookupResponse> getByTypeAndParentCode(String type, String parentCode, CatalogLookupFilterRequest request) {
        String normalizedParentCode = ReferenceCodeNormalizer.normalizeParentCode(parentCode);

        Pageable pageable = PageableUtils.limitOnly(
                request.limit(),
                "nameUz",
                Sort.Direction.ASC,
                DEFAULT_LOOKUP_LIMIT,
                MAX_LOOKUP_LIMIT
        );

        return catalogRepository.findAll(
                        CatalogSpecification.byTypeAndParentCodeAndName(type, normalizedParentCode, request.name()),
                        pageable
                )
                .map(catalogMapper::toLookupResponse)
                .getContent();
    }
}

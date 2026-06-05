package uz.uzinfocom.app.platform.reference.application.catalog.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.reference.application.catalog.query.dto.CatalogFilterRequest;
import uz.uzinfocom.app.platform.reference.application.catalog.query.dto.CatalogResponse;
import uz.uzinfocom.app.platform.reference.application.catalog.query.dto.CatalogTableResponse;
import uz.uzinfocom.app.platform.reference.application.catalog.query.mapper.CatalogMapper;
import uz.uzinfocom.app.platform.reference.application.catalog.query.projection.CatalogTableProjection;
import uz.uzinfocom.app.platform.reference.application.catalog.query.specification.CatalogSpecification;
import uz.uzinfocom.app.platform.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.platform.reference.application.lookup.ReferenceNameResolver;
import uz.uzinfocom.app.platform.reference.domain.Catalog;
import uz.uzinfocom.app.platform.reference.domain.enums.CatalogType;
import uz.uzinfocom.app.platform.reference.repository.CatalogRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogQueryService {

    private final CatalogRepository catalogRepository;
    private final CatalogMapper catalogMapper;
    private final ReferenceNameResolver referenceNameResolver;

    @Transactional(readOnly = true)
    public Page<CatalogTableResponse> findTable(CatalogFilterRequest request) {
        Pageable pageable = PageableUtils.of(request, CatalogSortFields.ALLOWED_SORT_FIELDS);

        Page<CatalogTableProjection> page = catalogRepository.findBy(
                CatalogSpecification.byFilter(request),
                query -> query
                        .as(CatalogTableProjection.class)
                        .page(pageable)
        );

        return page.map(projection -> catalogMapper.toTableResponse(projection, referenceNameResolver));
    }

    @Transactional(readOnly = true)
    public CatalogResponse getById(Long id) {
        return catalogRepository.findByIdAndDeletedFalse(id)
                .map(catalogMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("reference.catalog.not_found_by_id", id));
    }

    @Transactional(readOnly = true)
    public CatalogResponse getByTypeAndCode(CatalogType type, String code) {
        String normalizedCode = ReferenceCodeNormalizer.normalizeCode(code);

        return catalogRepository.findByTypeAndCodeAndDeletedFalse(type, normalizedCode)
                .map(catalogMapper::toResponse)
                .orElseThrow(() -> new NotFoundException(
                        "reference.catalog.not_found_by_type_code",
                        type,
                        normalizedCode
                ));
    }

    @Transactional(readOnly = true)
    public List<CatalogResponse> getByType(CatalogType type) {
        return catalogRepository.findAllByTypeAndDeletedFalseOrderBySortOrderAscNameUzAsc(type)
                .stream()
                .map(catalogMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogResponse> getByTypeAndParentCode(CatalogType type, String parentCode) {
        String normalizedParentCode = ReferenceCodeNormalizer.normalizeParentCode(parentCode);

        return catalogRepository
                .findAllByTypeAndParentCodeAndDeletedFalseOrderBySortOrderAscNameUzAsc(type, normalizedParentCode)
                .stream()
                .map(catalogMapper::toResponse)
                .toList();
    }
}

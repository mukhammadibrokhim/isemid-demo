package uz.uzinfocom.isemid.features.classifier.services.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.CatalogRequest;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogMapper;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.repositories.CatalogRepository;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.classifier.specifications.CatalogSpecification;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final CatalogRepository catalogRepository;
    private final MessageUtil messageUtil;
    private final CatalogMapper catalogMapper;

    @Override
    public Page<Catalog> filter(
            int page,
            int size,
            String type,
            String code,
            String parentCode,
            String nameRu,
            String nameUz,
            String nameUzCyril,
            String nameKaa
    ) {
        Specification<Catalog> spec = CatalogSpecification.build(
                type,
                code,
                parentCode,
                nameRu,
                nameUz,
                nameUzCyril,
                nameKaa
        );

        return catalogRepository.findAll(spec, pageableSortByIdDesc(page, size));
    }

    @Override
    @CacheEvict(
            value = {
                    "catalogCache",
                    "catalogAllType",
                    "catalogAllTypeSt",
                    "catalogAllTypeByParentCode",
                    "findByTypeAndParentCode",
                    "byParentCode",
                    "catalogType"
            },
            allEntries = true
    )
    public Catalog create(CatalogRequest request) {
        Catalog catalog = catalogMapper.toEntity(request);
        return catalogRepository.save(catalog);
    }

    @Override
    @CacheEvict(
            value = {
                    "catalogCache",
                    "catalogAllType",
                    "catalogAllTypeSt",
                    "catalogAllTypeByParentCode",
                    "findByTypeAndParentCode",
                    "byParentCode",
                    "catalogType"
            },
            allEntries = true
    )
    public Catalog update(long id, CatalogRequest request) {
        Catalog catalog = catalogRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        BAD_REQUEST,
                        messageUtil.get("error.not_found_by_id", id)
                ));

        catalogMapper.update(catalog, request);
        return catalogRepository.save(catalog);
    }

    @Override
    @CacheEvict(
            value = {
                    "catalogCache",
                    "catalogAllType",
                    "catalogAllTypeSt",
                    "catalogAllTypeByParentCode",
                    "findByTypeAndParentCode",
                    "byParentCode",
                    "catalogType"
            },
            allEntries = true
    )
    public void delete(long id) {
        Catalog catalog = catalogRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        BAD_REQUEST,
                        messageUtil.get("error.not_found_by_id", id)
                ));

        try {
            catalogRepository.delete(catalog);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(CONFLICT, messageUtil.get("error.cannot_delete_due_to_relations"), e);
        } catch (Exception e) {
            throw new ApiException(INTERNAL_SERVER_ERROR, messageUtil.get("error.internal_server"), e);
        }
    }

    @Override
    @Cacheable(value = "catalogCache", key = "#code", unless = "#result == null")
    public Catalog findByCode(String code) {
        return catalogRepository.findByCode(code).orElse(null);
    }

    @Override
    @Cacheable(value = "catalogAllType", key = "#catalogType.name()", unless = "#result == null")
    public List<Catalog> findAllByType(CatalogType catalogType) {
        return catalogRepository.findAllByType(catalogType.name());
    }

    @Override
    @Cacheable(
            value = "catalogAllTypeSt",
            key = "T(String).format('%s:%s', #catalogType, #search)",
            unless = "#result == null"
    )
    public List<Catalog> findAllByTypeString(String catalogType, String search) {
        return catalogRepository.searchByTypeAndName(catalogType, search);
    }

    @Override
    @Cacheable(
            value = "catalogAllTypeByParentCode",
            key = "T(String).format('%s:%s', #catalogType.name(), #parentCode)",
            unless = "#result == null"
    )
    public List<Catalog> findAllByTypeAndParentCode(CatalogType catalogType, String parentCode) {
        return catalogRepository.findAllByTypeAndParentCode(catalogType.name(), parentCode);
    }

    @Override
    @Cacheable(value = "byParentCode", key = "#parentCode", unless = "#result == null")
    public List<Catalog> findAllByParentCode(String parentCode) {
        return catalogRepository.findAllByParentCode(parentCode);
    }

    @Override
    public List<Catalog> findFirst20ByTypeAndSearch(CatalogType catalogType, String search) {
        return catalogRepository.findByTypeAndSearch(catalogType.name(), search);
    }

    @Override
    public Page<Catalog> getByHierarchical(int page, int size, CatalogType type) {
        Pageable pageable = PageRequest.of(page, size);
        return catalogRepository.findAllWithParent(type.name(), pageable);
    }

    @Override
    @Cacheable(
            value = "findByTypeAndParentCode",
            key = "T(String).format('%s:%s', #catalogType.name(), #parentCode)",
            unless = "#result == null || #result.isEmpty()"
    )
    public Optional<Catalog> findByTypeAndParentCode(CatalogType catalogType, String parentCode) {
        return catalogRepository.findByTypeAndParentCode(catalogType.name(), parentCode);
    }

    @Override
    public Optional<Catalog> findByTypeAndCode(CatalogType catalogType, String code) {
        return catalogRepository.findByTypeAndCode(catalogType.name(), code);
    }

    @Override
    @Cacheable(value = "catalogType", key = "#catalogType.name()", unless = "#result == null")
    public List<Catalog> finAllByType(CatalogType catalogType) {
        return catalogRepository.findAllByType(catalogType.name());
    }

    @Override
    public Catalog getById(long id) {
        return catalogRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        NOT_FOUND,
                        messageUtil.get("error.not_found_by_id", id)
                ));
    }
}
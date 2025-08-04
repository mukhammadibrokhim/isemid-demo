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
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.commons.utils.PagedResponseUtil;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.CatalogRequest;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogResponse;
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
    public PagedResponse<?> getFilteredCatalogs(int page, int size, String type, String code, String parentCode, String nameRu, String nameUz, String nameUzCyril, String nameKaa, String requestUrl) {
        Specification<Catalog> spec = CatalogSpecification.build(type, code, parentCode, nameRu, nameUz, nameUzCyril, nameKaa);
        Page<Catalog> catalogPage = catalogRepository.findAll(spec, pageableSortByIdDesc(page, size));
        return PagedResponseUtil.build(page, catalogPage, requestUrl, messageUtil);
    }

    @Override
    public ApiResponse<CatalogResponse> create(CatalogRequest request) {
        Catalog catalog = catalogMapper.toEntity(request);
        return ApiResponse.success(messageUtil.get("message.success"), catalogMapper.toDto(catalogRepository.save(catalog)));
    }

    @CacheEvict(value = {"catalogAllType", "catalogAllTypeByParentCode", "findByTypeAndParentCode"}, allEntries = true)
    @Override
    public ApiResponse<CatalogResponse> update(long id, CatalogRequest request) {
        Catalog catalog = catalogRepository.findById(id).orElseThrow(() -> new ApiException(BAD_REQUEST, messageUtil.get("error.not_found_by_id", id)));
        catalogMapper.update(catalog, request);
        return ApiResponse.success(messageUtil.get("message.success"), catalogMapper.toDto(catalogRepository.save(catalog)));
    }

    @Override
    @CacheEvict(value = {"catalogAllType", "catalogAllTypeByParentCode", "findByTypeAndParentCode"}, allEntries = true)
    public ApiResponse<?> delete(long id) {
        Catalog catalog = catalogRepository.findById(id)
                .orElseThrow(() -> new ApiException(BAD_REQUEST, messageUtil.get("error.not_found_by_id", id)));
        try {
            catalogRepository.delete(catalog);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(CONFLICT, messageUtil.get("error.cannot_delete_due_to_relations"), e);
        } catch (Exception e) {
            throw new ApiException(INTERNAL_SERVER_ERROR, messageUtil.get("error.internal_server"), e);
        }

        return ApiResponse.success(messageUtil.get("message.deleted"));
    }

    @Cacheable(value = "catalogCache", key = "#code")
    @Override
    public Catalog findByCode(String code) {
        return catalogRepository.findByCode(code).orElse(null);
    }

    @Cacheable(value = "catalogAllType", key = "T(String).format('%s-%s',#catalogType.name(),#lang)", unless = "#result == null")
    @Override
    public ApiResponse<?> findAllByType(CatalogType catalogType, String lang) {
        return ApiResponse.success(messageUtil.get("message.success"), catalogRepository.findAllByType(catalogType.name())
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @Cacheable(value = "catalogAllTypeByParentCode", key = "T(String).format('%s-%s-%s', #catalogType.name(), #parentCode, #lang)", unless = "#result == null")
    @Override
    public ApiResponse<?> findAllByTypeAndParentCode(CatalogType catalogType, String parentCode, String lang) {
        return ApiResponse.success(messageUtil.get("message.success"), catalogRepository.findAllByTypeAndParentCode(catalogType.name(), parentCode)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @Override
    public ApiResponse<?> findFirst20ByTypeAndSearch(CatalogType catalogType, String lang, String search) {
        return ApiResponse.success(messageUtil.get("message.success"), catalogRepository.findByTypeAndSearch(catalogType.name(), search)
                .stream().map(catalog -> catalogMapper.toDisplayedDto(catalog, lang)).toList());
    }

    @Override
    public PagedResponse<?> getByHierarchical(int page, int size, CatalogType type, String requestUrl) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Catalog> catalogPage = catalogRepository.findAllWithParent(type.name(), pageable);
        return PagedResponseUtil.build(page, catalogPage, requestUrl, messageUtil);
    }

    @Cacheable(value = "findByTypeAndParentCode", key = "T(String).format('%s-%s',#catalogType.name(), #parentCode)", unless = "#result == null")
    @Override
    public Optional<Catalog> findByTypeAndParentCode(CatalogType catalogType, String parentCode) {
        return catalogRepository.findByTypeAndParentCode(catalogType.name(), parentCode);
    }

    @Override
    public Optional<Catalog> findByTypeAndCode(CatalogType catalogType, String code) {
        return catalogRepository.findByTypeAndCode(catalogType.name(),code);
    }

    @Override
    public List<Catalog> finAllByType(CatalogType catalogType) {
        return catalogRepository.findAllByType(catalogType.name());
    }

    @Override
    public ApiResponse<CatalogResponse> getById(long id) {
        return ApiResponse.success(messageUtil.get("message.success"),
                catalogMapper.toDto(catalogRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)))));
    }
}
package uz.uzinfocom.isemid.features.classifier.services.impls;

import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.ApiResponsePage;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.CatalogRequest;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogResponse;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogMapper;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.repositories.CatalogRepository;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.classifier.specifications.CatalogSpecification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.http.HttpStatus.*;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.createApiResponsePage;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final CatalogRepository catalogRepository;
    private final ResourceLoader resourceLoader;
    private final CatalogMapper catalogMapper;

    @Override
    public ApiResponsePage getFilteredCatalogs(int page, int size, String type, String code, String parentCode, String nameRu, String nameUz, String nameUzCyril, String nameKaa, String requestUrl) {
        Specification<Catalog> spec = CatalogSpecification.getFilteredSpecification(type, code, parentCode, nameRu, nameUz, nameUzCyril, nameKaa);
        Page<Catalog> catalogPage = catalogRepository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, catalogPage, requestUrl);
    }

    @Override
    public ApiResponse<CatalogResponse> create(CatalogRequest reqCatalog) {
        Catalog catalog = Catalog.builder()
                .type(reqCatalog.getType())
                .code(reqCatalog.getCode())
                .parentCode(reqCatalog.getParentCode())
                .nameRu(reqCatalog.getNameRu())
                .nameUz(reqCatalog.getNameUz())
                .nameUzCyril(reqCatalog.getNameUzCyril())
                .nameKaa(reqCatalog.getNameKaa())
                .build();

        return new ApiResponse<>(true, MessageUtil.CREATED_SUCCESS, catalogMapper.toDto(catalogRepository.save(catalog)));
    }

    @CacheEvict(value = {"catalogAllType", "catalogAllTypeByParentCode", "findByTypeAndParentCode"}, allEntries = true)
    @Override
    public ApiResponse<CatalogResponse> update(long id, CatalogRequest request) {
        Catalog catalog = catalogRepository.findById(id).orElseThrow(() -> new ApiException(BAD_REQUEST, String.format(MessageUtil.NOT_FOUND_BY_RESOURCE, "Catalog", id)));
        Optional.ofNullable(request.getType()).ifPresent(catalog::setType);
        Optional.ofNullable(request.getCode()).ifPresent(catalog::setCode);
        Optional.ofNullable(request.getNameRu()).ifPresent(catalog::setNameRu);
        Optional.ofNullable(request.getNameUz()).ifPresent(catalog::setNameUz);
        Optional.ofNullable(request.getNameUzCyril()).ifPresent(catalog::setNameUzCyril);
        Optional.ofNullable(request.getNameKaa()).ifPresent(catalog::setNameKaa);
        Optional.ofNullable(request.getParentCode()).ifPresent(catalog::setParentCode);
        Optional.ofNullable(request.getType()).ifPresent(catalog::setType);
        return new ApiResponse<>(true, MessageUtil.UPDATED_SUCCESS, catalogMapper.toDto(catalogRepository.save(catalog)));
    }

    @CacheEvict(value = {"catalogAllType", "catalogAllTypeByParentCode", "findByTypeAndParentCode"}, allEntries = true)
    @Override
    public ApiResponse<?> delete(long id) {
        try {
            catalogRepository.deleteById(id);
        } catch (Exception e) {
            throw new ApiException(INTERNAL_SERVER_ERROR, "Could not delete catalog: ", e);
        }

        return new ApiResponse<>(true, "Successfully deleted.", null);
    }

    @Cacheable(value = "catalogCache", key = "#code")
    @Override
    public Catalog findByCode(String code) {
        return catalogRepository.findByCode(code).orElse(null);
    }

    @Cacheable(value = "catalogAllType", key = "T(String).format('%s-%s',#catalogType.name(),#lang)", unless = "#result == null")
    @Override
    public ApiResponse<?> findAllByType(CatalogType catalogType, String lang) {
        return new ApiResponse<>(true, MessageUtil.FETCHED_SUCCESS, catalogRepository.findAllByType(catalogType.name())
                .stream().map(catalog -> catalogMapper.toDtoMini(catalog, lang)).toList());
    }

    @Cacheable(value = "catalogAllTypeByParentCode", key = "T(String).format('%s-%s-%s', #catalogType.name(), #parentCode, #lang)", unless = "#result == null")
    @Override
    public ApiResponse<?> findAllByTypeAndParentCode(CatalogType catalogType, String parentCode, String lang) {
        return new ApiResponse<>(true, MessageUtil.FETCHED_SUCCESS, catalogRepository.findAllByTypeAndParentCode(catalogType.name(), parentCode)
                .stream().map(catalog -> catalogMapper.toDtoMini(catalog, lang)).toList());
    }

    @Override
    public ApiResponse<?> findFirst20ByTypeAndSearch(CatalogType catalogType, String lang, String search) {
        return new ApiResponse<>(true, MessageUtil.FETCHED_SUCCESS, catalogRepository.findByTypeAndSearch(catalogType.name(), search)
                .stream().map(catalog -> catalogMapper.toDtoMini(catalog, lang)).toList());
    }

    @Override
    public ApiResponsePage getByHierarchical(int page, int size, CatalogType type, String requestUrl) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Catalog> catalogPage = catalogRepository.findAllWithParent(type.name(), pageable);
        return createApiResponsePage(page, catalogPage, requestUrl);
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
        return new ApiResponse<>(true, MessageUtil.FETCHED_SUCCESS,
                catalogMapper.toDto(catalogRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, String.format(MessageUtil.NOT_FOUND_BY_ID, id)))));
    }

    @SneakyThrows
    @PostConstruct
    private void insertCatalog() {
        try {
            Resource resource = resourceLoader.getResource("classpath:/base/catalog.csv");
            InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(reader);

            try (CSVReader csvReader = new CSVReader(bufferedReader)) {
                List<String[]> records = csvReader.readAll();

                Set<String> existingCodes = catalogRepository.findAllCodes();

                List<Catalog> newCatalogs = records.stream()
                        .skip(1)
                        .map(record -> Catalog.builder()
                                .code(record[7])
                                .nameKaa(record[8])
                                .nameRu(record[9])
                                .nameUz(record[10])
                                .nameUzCyril(record[11])
                                .parentCode(record[12])
                                .type(record[13])
                                .build())
                        .filter(catalog -> !existingCodes.contains(catalog.getCode()))
                        .toList();

                if (!newCatalogs.isEmpty()) {
                    catalogRepository.saveAll(newCatalogs);
                }
            }
        } catch (IOException e) {
            log.error("Exception: {}",e.getMessage());
            throw new ApiException(INTERNAL_SERVER_ERROR, MessageUtil.OPERATION_FAILED, e);
        }
    }
}
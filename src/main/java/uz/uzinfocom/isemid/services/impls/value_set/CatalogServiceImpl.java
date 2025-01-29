package uz.uzinfocom.isemid.services.impls.value_set;

import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.enums.CatalogType;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;
import uz.uzinfocom.isemid.models.payload.requests.ReqCatalog;
import uz.uzinfocom.isemid.models.payload.responses.RespCatalog;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.value_set.CatalogRepository;
import uz.uzinfocom.isemid.services.value_set.CatalogService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.utils.CommonUtils.*;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    final CatalogRepository catalogRepository;
    final ResourceLoader resourceLoader;
    final static String MESSAGE = "Request completed successfully";

    @Override
    public ApiResponse<Catalog> createCatalog(ReqCatalog reqCatalog) {
        Catalog catalog = Catalog.builder()
                .type(reqCatalog.getType())
                .code(reqCatalog.getCode())
                .parentCode(reqCatalog.getParentCode())
                .nameRu(reqCatalog.getNameRu())
                .nameUz(reqCatalog.getNameUz())
                .nameUzCyril(reqCatalog.getNameUzCyril())
                .nameKaa(reqCatalog.getNameKaa())
                .build();
        return new ApiResponse<>(true, "Catalog successfully created.", catalogRepository.save(catalog));
    }

    @Override
    public ApiResponse<Catalog> updateCatalogById(long id, ReqCatalog reqCatalog) {
        Catalog catalog = catalogRepository.findById(id).orElseThrow(() -> new ApiException(BAD_REQUEST, "Catalog not found for id: " + id));
        Optional.ofNullable(reqCatalog.getCode()).ifPresent(catalog::setCode);
        Optional.ofNullable(reqCatalog.getNameRu()).ifPresent(catalog::setNameRu);
        Optional.ofNullable(reqCatalog.getNameUz()).ifPresent(catalog::setNameUz);
        Optional.ofNullable(reqCatalog.getNameUzCyril()).ifPresent(catalog::setNameUzCyril);
        Optional.ofNullable(reqCatalog.getNameKaa()).ifPresent(catalog::setNameKaa);
        Optional.ofNullable(reqCatalog.getParentCode()).ifPresent(catalog::setParentCode);
        Optional.ofNullable(reqCatalog.getType()).ifPresent(catalog::setType);
        return new ApiResponse<>(true, "Successfully updated.", catalogRepository.save(catalog));
    }

    @Cacheable(value = "catalogAllType", key = "T(String).format('%s-%s',#catalogType.name(),#lang)", unless = "#result == null")
    @Override
    public ApiResponse<List<RespCatalog>> findAllByType(CatalogType catalogType, String lang) {
        return new ApiResponse<>(true, MESSAGE, catalogRepository.findAllByType(catalogType.name())
                .stream()
                .map(data -> RespCatalog.builder()
                        .code(data.getCode())
                        .id(data.getId())
                        .parentCode(data.getParentCode())
                        .display(resolveLang(data.getNameRu(), data.getNameUz(), data.getNameUzCyril(), data.getNameKaa()))
                        .build()).toList());
    }

    @Override
    public ApiResponsePage getAllCatalogs(int page, int size, String type, String code, String parentCode, String nameRu, String nameUz, String nameUzCyril, String nameKaa, HttpServletRequest request) {
        Specification<Catalog> spec = getFilteredSpecification(type, code, parentCode, nameRu, nameUz, nameUzCyril, nameKaa);
        Page<Catalog> catalogPage = catalogRepository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, catalogPage, getFullURL(request));
    }

    @Cacheable(value = "catalogAllTypeByParentCode", key = "T(String).format('%s-%s-%s', #catalogType.name(), #parentCode, #lang)", unless = "#result == null")
    @Override
    public ApiResponse<List<RespCatalog>> findAllByTypeAndParentCode(CatalogType catalogType, String parentCode, String lang) {
        return new ApiResponse<>(true, MESSAGE, catalogRepository.findAllByTypeAndParentCode(catalogType.name(), parentCode)
                .stream().map(data -> RespCatalog.builder()
                        .id(data.getId())
                        .parentCode(data.getParentCode())
                        .code(data.getCode())
                        .display(resolveLang(data.getNameRu(), data.getNameUz(), data.getNameUzCyril(), data.getNameKaa()))
                        .build()).toList());
    }

    @Override
    public ApiResponse<List<RespCatalog>> findFirst20ByTypeAndSearch(CatalogType type, String lang, String search) {
        List<Catalog> resultList = Optional.ofNullable(search)
                .filter(StringUtils::hasText)
                .map(s -> catalogRepository.findByTypeAndSearch(type.name(), s))
                .orElseGet(() -> catalogRepository.findByTypeAndSearch(type.name(), null));

        List<RespCatalog> response = resultList.stream()
                .map(catalog -> RespCatalog.builder()
                        .id(catalog.getId())
                        .code(catalog.getCode())
                        .parentCode(catalog.getParentCode())
                        .display(resolveLang(catalog.getNameRu(), catalog.getNameUz(), catalog.getNameUzCyril(), catalog.getNameKaa()))
                        .build())
                .collect(Collectors.toList());

        return new ApiResponse<>(true, "Successfully fetch", response);
    }

    @Cacheable(value = "findByTypeAndParentCode", key = "T(String).format('%s-%s',#catalogType.name(), #parentCode)", unless = "#result == null")
    @Override
    public Optional<Catalog> findByTypeAndParentCode(CatalogType catalogType, String parentCode) {
        return catalogRepository.findByTypeAndParentCode(catalogType.name(), parentCode);
    }

    @Override
    public Optional<Catalog> findByTypeAndCode(CatalogType catalogType, String code) {
        return catalogRepository.findByTypeAndCode(catalogType.name(), code);
    }

    public static Specification<Catalog> getFilteredSpecification(
            String type,
            String code,
            String parentCode,
            String nameRu,
            String nameUz,
            String nameUzCyril,
            String nameKaa
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (hasText(type)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("type")), "%" + type.toLowerCase() + "%"));
            }
            if (hasText(code)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), "%" + code.toLowerCase() + "%"));
            }
            if (hasText(parentCode)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("parentCode")), "%" + parentCode.toLowerCase() + "%"));
            }
            if (hasText(nameRu)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nameRu")), "%" + nameRu.toLowerCase() + "%"));
            }
            if (hasText(nameUz)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nameUz")), "%" + nameUz.toLowerCase() + "%"));
            }
            if (hasText(nameUzCyril)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nameUzCyril")), "%" + nameUzCyril.toLowerCase() + "%"));
            }
            if (hasText(nameKaa)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nameKaa")), "%" + nameKaa.toLowerCase() + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @SneakyThrows
    @PostConstruct
    private void insertCatalog() {
        Resource resource = resourceLoader.getResource("classpath:/base/catalog_isemid.csv");
        InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(reader);

        try (CSVReader csvReader = new CSVReader(bufferedReader)) {
            List<String[]> records = csvReader.readAll();
            List<Catalog> identifierTypes = records.stream()
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
                    .filter(catalogCode -> catalogRepository.findAllByCode(catalogCode.getCode()).isEmpty())
                    .collect(Collectors.toList());
            if (!identifierTypes.isEmpty()) {
                catalogRepository.saveAll(identifierTypes);
            }
        }
    }
}

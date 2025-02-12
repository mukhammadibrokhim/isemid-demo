package uz.uzinfocom.isemid.services.impls.value_set;

import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
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

    @Override
    public ApiResponse<List<RespCatalog>> findByProfessionTypeAndSearch(CatalogType professionType, String lang, String search) {
        List<Catalog> resultList = Optional.ofNullable(search)
                .filter(StringUtils::hasText)
                .map(s -> catalogRepository.findByProfessionTypeAndSearch(professionType.name(), s))
                .orElseGet(() -> catalogRepository.findByProfessionTypeAndSearch(professionType.name(), null));

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

    @Override
    public ApiResponse<?> deleteById(long id) {
        try {
            catalogRepository.deleteById(id);
        } catch (Exception e) {
            throw new ApiException(INTERNAL_SERVER_ERROR, "Could not delete catalog: ", e);
        }

        return new ApiResponse<>(true, "Successfully deleted.", null);
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

//    @SneakyThrows
//    @PostConstruct
//    private void insertCatalog() {
//        Resource resource = resourceLoader.getResource("classpath:/base/catalog.csv");
//        InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
//        BufferedReader bufferedReader = new BufferedReader(reader);
//
//        try (CSVReader csvReader = new CSVReader(bufferedReader)) {
//            List<String[]> records = csvReader.readAll();
//            List<Catalog> identifierTypes = records.stream()
//                    .skip(1)
//                    .map(record -> Catalog.builder()
//                            .code(record[7])
//                            .nameKaa(record[8])
//                            .nameRu(record[9])
//                            .nameUz(record[10])
//                            .nameUzCyril(record[11])
//                            .parentCode(record[12])
//                            .type(record[13])
//                            .build())
//                    .filter(catalogCode -> catalogRepository.findAllByCode(catalogCode.getCode()).isEmpty())
//                    .collect(Collectors.toList());
//            if (!identifierTypes.isEmpty()) {
//                catalogRepository.saveAll(identifierTypes);
//            }
//        }
//    }

//    @PostConstruct
//    private void insertNeighborhood() throws IOException {
//        Resource resource = new DefaultResourceLoader().getResource("classpath:/base/rb_administrative_territory.json");
//        Map mapLoinc = new ObjectMapper().readValue(resource.getInputStream(), Map.class);
//        List<Map> mapList = (ArrayList<Map>) mapLoinc.get("regions");
//
//        List<Map> level2RegionMapList = mapList.stream()
//                .filter(record -> "2".equals(String.valueOf(record.get("level"))))
//                .toList();
//
//        List<Map> level3RegionMapList = mapList.stream()
//                .filter(record -> "3".equals(String.valueOf(record.get("level"))))
//                .toList();
//
//        Set<String> existingCodes = catalogRepository.findAll().stream()
//                .map(Catalog::getCode)
//                .collect(Collectors.toSet());
//
//        List<Catalog> level2Catalogs = level2RegionMapList.stream()
//                .map(record -> {
//                    String code = String.valueOf(record.get("code"));
//                    String nameUz = String.valueOf(record.get("name_uz"));
//                    String parentCode = record.get("parent_code") != null
//                            ? String.valueOf(record.get("parent_code"))
//                            : "UZ-" + (code.length() >= 2 ? code.substring(0, 2) : code);
//
//                    return Catalog.builder()
//                            .code(code)
//                            .nameKaa(nameUz)
//                            .nameRu(nameUz)
//                            .nameUz(nameUz)
//                            .nameUzCyril(nameUz)
//                            .parentCode(parentCode)
//                            .type(CatalogType.city.name())
//                            .build();
//                })
//                .filter(catalog -> !existingCodes.contains(catalog.getCode()))
//                .toList();
//        List<Catalog> level3Catalogs = level3RegionMapList.stream()
//                .map(record -> {
//                    String code = String.valueOf(record.get("code"));
//                    return Catalog.builder()
//                            .code(code)
//                            .nameKaa(String.valueOf(record.get("name_uz")))
//                            .nameRu(String.valueOf(record.get("name_uz")))
//                            .nameUz(String.valueOf(record.get("name_uz")))
//                            .nameUzCyril(String.valueOf(record.get("name_uz")))
//                            .parentCode(String.valueOf(record.get("parent_code")))
//                            .type(CatalogType.neighborhood.name())
//                            .build();
//                })
//                .filter(catalog -> !existingCodes.contains(catalog.getCode()))
//                .toList();
//        List<Catalog> allCatalogs = new ArrayList<>();
//        allCatalogs.addAll(level2Catalogs);
//        allCatalogs.addAll(level3Catalogs);
//
//        if (!allCatalogs.isEmpty()) {
//            catalogRepository.saveAll(allCatalogs);
//        }
//    }
//
//
//    @PostConstruct
//    @SneakyThrows
//    private void insertProfessions() {
//        Resource resource = resourceLoader.getResource("classpath:/base/rb_professions.json");
//        InputStream inputStream = resource.getInputStream();
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode rootNode = objectMapper.readTree(inputStream);
//        JsonNode professions = rootNode.get("professions");
//
//        if (professions != null && professions.isArray()) {
//            List<Catalog> professionList = StreamSupport.stream(professions.spliterator(), false)
//                    .map(record -> Catalog.builder()
//                            .code(record.get("code").asText())
//                            .nameRu(record.get("name_ru").asText())
//                            .nameUz(record.get("name_uz").asText())
//                            .nameUzCyril(record.get("name_uz_cyrl").asText())
//                            .nameKaa(record.get("name_ru").asText())
//                            .parentCode(null)
//                            .type(CatalogType.profession.name())
//                            .build())
//                    .filter(catalog -> catalogRepository.findAllByCode(catalog.getCode()).isEmpty())
//                    .collect(Collectors.toList());
//
//            if (!professionList.isEmpty()) {
//                catalogRepository.saveAll(professionList);
//            }
//        }
//    }
}
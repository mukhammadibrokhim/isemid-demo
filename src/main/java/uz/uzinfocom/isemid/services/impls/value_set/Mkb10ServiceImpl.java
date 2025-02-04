package uz.uzinfocom.isemid.services.impls.value_set;

import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.value_set.Mkb10;
import uz.uzinfocom.isemid.models.payload.requests.ReqMkb10;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.value_set.Mkb10Repository;
import uz.uzinfocom.isemid.services.value_set.Mkb10Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.utils.CommonUtils.createApiResponsePage;
import static uz.uzinfocom.isemid.utils.CommonUtils.pageableNotSorting;

@Service
@RequiredArgsConstructor
public class Mkb10ServiceImpl implements Mkb10Service {

    final Mkb10Repository mkb10Repository;
    final ResourceLoader resourceLoader;

    @Override
    public ApiResponsePage getMkb10ByFilter(int page, int size, String mkbCode, String nameUz, String nameUzCyril, String nameRu, String nameEn, String fullURL) {
        Specification<Mkb10> spec = getFilteredSpecification(mkbCode, nameUz, nameUzCyril, nameRu, nameEn);
        Page<Mkb10> mkb10Page = mkb10Repository.findAll(spec, pageableNotSorting(page, size));
        return createApiResponsePage(page, mkb10Page, fullURL);
    }

    @Override
    public ApiResponse<?> getById(long id) {
        return new ApiResponse<>(true, "Success",
                mkb10Repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Mkb10 not found.")));
    }

    @Override
    public ApiResponse<?> updateById(long id, ReqMkb10 reqMkb10, User user) {
        return new ApiResponse<>(true, "Successfully update.", null);
    }

    @Override
    public ApiResponse<?> deleteById(long id) {
        try {
            mkb10Repository.deleteById(id);
        } catch (Exception e) {
            throw new ApiException(NOT_FOUND, "Mkb10 not found.", e);
        }
        return new ApiResponse<>(true, "Successfully deleted.", null);
    }

    @Override
    @Cacheable(value = "parentNodesCache",
            key = "'parentNodesCache'",
            unless = "#result == null")
    public ApiResponse<?> getParentNodes() {
        return new ApiResponse<>(true,
                "Successfully retrieved parent nodes.", mkb10Repository.findByParentIdIsNull());
    }

    @Override
    public ApiResponse<?> getParentWithChildren() {
        return new ApiResponse<>(true, "Successfully retrieved parent nodes.",  mkb10Repository.findByParentIdIsNull());
    }

    public static Specification<Mkb10> getFilteredSpecification(
            String mkbCode,
            String nameUz,
            String nameUzCyril,
            String nameRu,
            String nameEn
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isNull(root.get("parentId")));
            if (hasText(mkbCode)) {
                predicates.add(criteriaBuilder.equal(root.get("code"), mkbCode));
            }
            if (hasText(nameUz)) {
                criteriaBuilder.like(criteriaBuilder.lower(root.get("nameUz")), "%" + nameUz + "%");
            }
            if (hasText(nameUzCyril)) {
                criteriaBuilder.like(criteriaBuilder.lower(root.get("nameUzCyrl")), "%" + nameUzCyril + "%");
            }
            if (hasText(nameRu)) {
                criteriaBuilder.like(criteriaBuilder.lower(root.get("nameRu")), "%" + nameRu + "%");
            }
            if (hasText(nameEn)) {
                criteriaBuilder.like(criteriaBuilder.lower(root.get("nameEn")), "%" + nameEn + "%");
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }


    @SneakyThrows
    @PostConstruct
    private void insertStreet() {
        Resource resource = resourceLoader.getResource("classpath:/base/mkb10.csv");
        InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(reader);
        try (CSVReader csvReader = new CSVReader(bufferedReader)) {
            List<String[]> records = csvReader.readAll();
            Map<Long, Mkb10> mkb10Map = records.stream()
                    .skip(1)
                    .map(record -> Mkb10.builder()
                            .id(Long.parseLong(record[1]))           // ID
                            .code(record[2]) // code
                            .isLastLevel(Boolean.parseBoolean(record[3])) // is_last_level
                            .level(Integer.parseInt(record[4])) // level
                            .nameKaa(record[6]) // name_en
                            .nameRu(record[6]) // name_ru
                            .nameUz(record[7]) // name_uz
                            .nameUzCyril(record[8]) // name_uz_cyrl
                            .parentId(record[9].isEmpty() ? null : Long.parseLong(record[9])) // parent_id
                            .secondaryId(Long.parseLong(record[10])) // secondary_id
                            .build())
                    .filter(mkb10 -> mkb10Repository.findAllByCode(mkb10.getCode()).isEmpty())
                    .collect(Collectors.toMap(Mkb10::getId, mkb10 -> mkb10));
            mkb10Repository.saveAll(mkb10Map.values());
        }
    }

//    @SneakyThrows
//    @PostConstruct
//    private void insertStreet() {
//        Resource resource = resourceLoader.getResource("classpath:/base/mkb10.csv");
//        InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
//        BufferedReader bufferedReader = new BufferedReader(reader);
//        try (CSVReader csvReader = new CSVReader(bufferedReader)) {
//            List<String[]> records = csvReader.readAll();
//            Map<Long, Mkb10> mkb10Map = records.stream()
//                    .skip(1)
//                    .map(record -> Mkb10.builder()
//                            .id(Long.parseLong(record[1]))           // ID
//                            .code(record[2]) // code
//                            .isLastLevel(Boolean.parseBoolean(record[3])) // is_last_level
//                            .level(Integer.parseInt(record[4])) // level
//                            .nameEn(record[5]) // name_en
//                            .nameRu(record[6]) // name_ru
//                            .nameUz(record[7]) // name_uz
//                            .nameUzCyrl(record[8]) // name_uz_cyrl
//                            .parentId(record[9].isEmpty() ? null : Long.parseLong(record[9])) // parent_id
//                            .secondaryId(Long.parseLong(record[10])) // secondary_id
//                            .build())
//                    .collect(Collectors.toMap(Mkb10::getId, mkb10 -> mkb10));
//
//            mkb10Map.values().forEach(mkb10 -> {
//                if (mkb10.getParentId() != null) {
//                    Mkb10 parent = mkb10Map.get(mkb10.getParentId());
//                    mkb10.setParent(parent);
//                    if (parent != null) {
//                        if (parent.getChildren() == null) {
//                            parent.setChildren(new ArrayList<>()); // NullPointerException dan saqlanish
//                        }
//                        parent.getChildren().add(mkb10);
//                    }
//                }
//            });
//
//            mkb10Repository.saveAll(mkb10Map.values());
//        }
//    }
}

package uz.uzinfocom.isemid.services.impls.value_set;

import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.value_set.Mkb10;
import uz.uzinfocom.isemid.models.payload.requests.value_set.ReqMkb10;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.value_set.Mkb10Repository;
import uz.uzinfocom.isemid.services.value_set.Mkb10Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
    public ApiResponse<?> updateById(long id, ReqMkb10 reqMkb10) {
        Mkb10 existedMkb10 = mkb10Repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Mkb10 not found."));
        Optional.ofNullable(reqMkb10.getCode()).ifPresent(existedMkb10::setCode);
        Optional.ofNullable(reqMkb10.getNameUz()).ifPresent(existedMkb10::setNameUz);
        Optional.ofNullable(reqMkb10.getNameRu()).ifPresent(existedMkb10::setNameRu);
        Optional.ofNullable(reqMkb10.getNameKaa()).ifPresent(existedMkb10::setNameKaa);
        Optional.ofNullable(reqMkb10.getNameUzCyril()).ifPresent(existedMkb10::setNameUzCyril);
        Optional.ofNullable(reqMkb10.getComment()).ifPresent(existedMkb10::setComment);
        Optional.ofNullable(reqMkb10.getUsageLimit()).ifPresent(existedMkb10::setUsageLimit);
        return new ApiResponse<>(true, "Successfully update.", mkb10Repository.save(existedMkb10));
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
    public ApiResponse<?> getAllMkb10s(Long parentId, String search) {
        if (search == null || search.isEmpty()) {
            search = null;
        }
        List<Mkb10> results = mkb10Repository.searchMkb10(parentId, search);
        return new ApiResponse<>(true, "Success", results);
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

            Set<String> allCodes = mkb10Repository.findAllCodes();

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
                    .filter(mkb10 -> !allCodes.contains(mkb10.getCode()))
                    .collect(Collectors.toMap(Mkb10::getId, mkb10 -> mkb10));
            mkb10Repository.saveAll(mkb10Map.values());
        }
    }
}

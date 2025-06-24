package uz.uzinfocom.isemid.features.classifier.services.impls;

import com.opencsv.CSVReader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.Mkb10Request;
import uz.uzinfocom.isemid.features.classifier.models.Mkb10;
import uz.uzinfocom.isemid.features.classifier.repositories.Mkb10Repository;
import uz.uzinfocom.isemid.features.classifier.services.Mkb10Service;
import uz.uzinfocom.isemid.features.classifier.specifications.Mkb10Specification;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.createApiResponsePage;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableNotSorting;

@Service
@RequiredArgsConstructor
public class Mkb10ServiceImpl implements Mkb10Service {

    private final Mkb10Repository mkb10Repository;
    private final ResourceLoader resourceLoader;

    @Override
    public PagedResponse<?> getMkb10ByFilter(int page, int size, String mkbCode, Long parentId, String nameUz, String nameUzCyril, String nameRu, String nameEn, String fullURL) {
        Specification<Mkb10> spec = Mkb10Specification.getFilteredSpecification(mkbCode, parentId, nameUz, nameUzCyril, nameRu, nameEn);
        Page<Mkb10> mkb10Page = mkb10Repository.findAll(spec, pageableNotSorting(page, size));
        return createApiResponsePage(page, mkb10Page, fullURL);
    }

    @Override
    public ApiResponse<?> getById(long id) {
        return new ApiResponse<>(true, MessageUtil.FETCHED_SUCCESS,
                mkb10Repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, MessageUtil.NOT_FOUND)));
    }

    @Override
    public ApiResponse<?> update(long id, Mkb10Request reqMkb10) {
        Mkb10 existedMkb10 = mkb10Repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, MessageUtil.NOT_FOUND));
        Optional.ofNullable(reqMkb10.getCode()).ifPresent(existedMkb10::setCode);
        Optional.ofNullable(reqMkb10.getNameUz()).ifPresent(existedMkb10::setNameUz);
        Optional.ofNullable(reqMkb10.getNameRu()).ifPresent(existedMkb10::setNameRu);
        Optional.ofNullable(reqMkb10.getNameKaa()).ifPresent(existedMkb10::setNameKaa);
        Optional.ofNullable(reqMkb10.getNameUzCyril()).ifPresent(existedMkb10::setNameUzCyril);
        Optional.ofNullable(reqMkb10.getComment()).ifPresent(existedMkb10::setComment);
        Optional.ofNullable(reqMkb10.getUsageLimit()).ifPresent(existedMkb10::setUsageLimit);
        return new ApiResponse<>(true, MessageUtil.UPDATED_SUCCESS, mkb10Repository.save(existedMkb10));
    }

    @Override
    public ApiResponse<?> delete(long id) {
        try {
            mkb10Repository.deleteById(id);
        } catch (Exception e) {
            throw new ApiException(INTERNAL_SERVER_ERROR,MessageUtil.OPERATION_FAILED, e);
        }
        return new ApiResponse<>(true, MessageUtil.DELETED_SUCCESS, null);
    }

    @Override
    public ApiResponse<?> getAllMkb10s(Long parentId, String search) {
        if (search == null || search.isEmpty()) {
            search = null;
        }
        List<Mkb10> results = mkb10Repository.searchMkb10(parentId, search);
        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, results);
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
                            .isLastLevel(Boolean.parseBoolean(record[6])) // is_last_level
                            .level(Integer.parseInt(record[7])) // level
                            .nameKaa(record[8]) // name_en
                            .nameRu(record[8]) // name_ru
                            .nameUz(record[10]) // name_uz
                            .nameUzCyril(record[11]) // name_uz_cyrl
                            .parentId(record[12].isEmpty() ? null : Long.parseLong(record[12])) // parent_id
                            .secondaryId(Long.parseLong(record[13])) // secondary_id
                            .usageLimit(Integer.parseInt(record[16]))
                            .build())
                    .filter(mkb10 -> !allCodes.contains(mkb10.getCode()))
                    .collect(Collectors.toMap(Mkb10::getId, mkb10 -> mkb10));
            mkb10Repository.saveAll(mkb10Map.values());
        }
    }
}

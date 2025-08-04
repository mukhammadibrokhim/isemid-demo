package uz.uzinfocom.isemid.features.classifier.services.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.commons.utils.PagedResponseUtil;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.Mkb10Request;
import uz.uzinfocom.isemid.features.classifier.mappers.Mkb10Mapper;
import uz.uzinfocom.isemid.features.classifier.models.Mkb10;
import uz.uzinfocom.isemid.features.classifier.repositories.Mkb10Repository;
import uz.uzinfocom.isemid.features.classifier.services.Mkb10Service;
import uz.uzinfocom.isemid.features.classifier.specifications.Mkb10Specification;

import java.util.List;

import static org.springframework.http.HttpStatus.*;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableNotSorting;

@Service
@RequiredArgsConstructor
public class Mkb10ServiceImpl implements Mkb10Service {
    private final Mkb10Repository mkb10Repository;
    private final Mkb10Mapper mapper;
    private final MessageUtil messageUtil;

    @Override
    public PagedResponse<?> getMkb10ByFilter(int page, int size, String mkbCode, Long parentId, String nameUz, String nameUzCyril, String nameRu, String nameEn, String fullURL) {
        Specification<Mkb10> spec = Mkb10Specification.build(mkbCode, parentId, nameUz, nameUzCyril, nameRu, nameEn);
        Page<Mkb10> mkb10Page = mkb10Repository.findAll(spec, pageableNotSorting(page, size));
        return PagedResponseUtil.build(page, mkb10Page, fullURL, messageUtil);
    }

    @Override
    public ApiResponse<?> getById(long id) {
        return ApiResponse.success(messageUtil.get("message.success"),
                mkb10Repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id))));
    }

    @Override
    public ApiResponse<?> update(long id, Mkb10Request request) {
        Mkb10 existedMkb10 = mkb10Repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
        mapper.update(existedMkb10, request);
        return ApiResponse.success(messageUtil.get("message.success"), mkb10Repository.save(existedMkb10));
    }

    @Transactional
    @Override
    public ApiResponse<?> delete(long id) {
        Mkb10 mkb10 = mkb10Repository.findById(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));

        try {
            mkb10Repository.delete(mkb10);
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(CONFLICT, messageUtil.get("error.cannot_delete_due_to_relations"));
        } catch (Exception e) {
            throw new ApiException(INTERNAL_SERVER_ERROR, messageUtil.get("error.internal_server"));
        }
        return ApiResponse.success(messageUtil.get("message.success"));
    }

    @Override
    public ApiResponse<?> getAllMkb10s(Long parentId, String search) {
        if (search == null || search.isEmpty()) {
            search = null;
        }
        List<Mkb10> results = mkb10Repository.searchMkb10(parentId, search);
        return ApiResponse.success(messageUtil.get("message.success"), results);
    }

}

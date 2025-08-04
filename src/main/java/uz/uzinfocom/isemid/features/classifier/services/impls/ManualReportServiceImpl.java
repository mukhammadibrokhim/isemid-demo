package uz.uzinfocom.isemid.features.classifier.services.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.commons.utils.PagedResponseUtil;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.ManualReportRequest;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.ManualReportResponse;
import uz.uzinfocom.isemid.features.classifier.mappers.ManualReportMapper;
import uz.uzinfocom.isemid.features.classifier.models.ManualReport;
import uz.uzinfocom.isemid.features.classifier.repositories.ManualReportRepository;
import uz.uzinfocom.isemid.features.classifier.services.ManualReportService;
import uz.uzinfocom.isemid.features.classifier.specifications.ManualReportSpecification;

import static org.springframework.http.HttpStatus.*;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableNotSorting;

@Service
@RequiredArgsConstructor
public class ManualReportServiceImpl implements ManualReportService {
    private final ManualReportRepository repo;
    private final MessageUtil messageUtil;
    private final ManualReportMapper mapper;

    @Override
    public PagedResponse<?> getAllByFilter(int page, int size, String shortName, String nameUz, String nameUzCyril, String nameRu, String nameKaa, String code, Boolean includeTotal, String fullURL) {
        Specification<ManualReport> spec = ManualReportSpecification.build(shortName, nameUz, nameUzCyril, nameRu, nameKaa, code, includeTotal);
        Page<ManualReportResponse> mapped = repo.findAll(spec, pageableNotSorting(page, size))
                .map(mapper::toResponse);
        return PagedResponseUtil.build(page, mapped, fullURL, messageUtil);
    }

    @Override
    public ApiResponse<?> create(ManualReportRequest request) {
        ManualReport manualReport = mapper.toEntity(request);
        return ApiResponse.success(messageUtil.get("message.success"), mapper.toResponse(repo.save(manualReport)));
    }

    @Override
    public ApiResponse<?> getById(long id) {
        ManualReport manualReport = repo.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
        return ApiResponse.success(messageUtil.get("message.success"), mapper.toResponse(manualReport));
    }

    @Override
    public ApiResponse<?> delete(long id) {
        ManualReport report = repo.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));

        try {
            repo.delete(report);
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(CONFLICT, messageUtil.get("error.cannot_delete_due_to_relations"));
        } catch (Exception ex) {
            throw new ApiException(INTERNAL_SERVER_ERROR, messageUtil.get("error.internal_server"), ex);
        }
        return ApiResponse.success(messageUtil.get("message.deleted"));
    }

    @Override
    public ApiResponse<?> update(long id, ManualReportRequest request) {
        ManualReport manualReport = repo.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
        mapper.update(manualReport, request);
        return ApiResponse.success(messageUtil.get("message.success"), mapper.toResponse(repo.save(manualReport)));
    }
}

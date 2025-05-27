package uz.uzinfocom.isemid.features.classifier.services.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.ApiResponsePage;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.ManualReportRequest;
import uz.uzinfocom.isemid.features.classifier.mappers.ManualReportMapper;
import uz.uzinfocom.isemid.features.classifier.models.ManualReport;
import uz.uzinfocom.isemid.features.classifier.repositories.ManualReportRepository;
import uz.uzinfocom.isemid.features.classifier.services.ManualReportService;
import uz.uzinfocom.isemid.features.classifier.specifications.ManualReportSpecification;

import java.util.HashSet;
import java.util.Optional;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.createApiResponsePage;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableNotSorting;

@Service
@RequiredArgsConstructor
public class ManualReportServiceImpl implements ManualReportService {
    private final ManualReportRepository repo;
    private final ManualReportMapper mapper;

    @Override
    public ApiResponsePage getAllByFilter(int page, int size, String shortName, String nameUz, String nameUzCyril, String nameRu, String nameKaa, String code, Boolean includeTotal, String fullURL) {
        Specification<ManualReport> spec = ManualReportSpecification.getFilteredSpecification(shortName, nameUz, nameUzCyril, nameRu, nameKaa, code, includeTotal);
        Page<ManualReport> mkb10Page = repo.findAll(spec, pageableNotSorting(page, size));
        return createApiResponsePage(page, mkb10Page.map(mapper::toResponse), fullURL);
    }

    @Override
    public ApiResponse<?> create(ManualReportRequest reqManualReport) {
        ManualReport manualReport = ManualReport.builder()
                .code(reqManualReport.getCode())
                .shortName(reqManualReport.getShortName())
                .nameUz(reqManualReport.getNameUz())
                .nameUzCyril(reqManualReport.getNameUzCyril())
                .nameRu(reqManualReport.getNameRu())
                .nameKaa(reqManualReport.getNameKaa())
                .includeInTotal(reqManualReport.getIncludeInTotal())
                .mkb10Codes(reqManualReport.getMkb10Codes())
                .build();

        return new ApiResponse<>(true, MessageUtil.CREATED_SUCCESS, mapper.toResponse(repo.save(manualReport)));
    }

    @Override
    public ApiResponse<?> getById(long id) {
        ManualReport manualReport = repo.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, String.format(MessageUtil.NOT_FOUND_BY_ID, id)));
        return new ApiResponse<>(true, MessageUtil.FETCHED_SUCCESS, mapper.toResponse(manualReport));
    }

    @Override
    public ApiResponse<?> delete(long id) {
        try {
            repo.deleteById(id);
        } catch (Exception e) {
            throw new ApiException(INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        }
        return new ApiResponse<>(true, MessageUtil.DELETED_SUCCESS, null);
    }

    @Override
    public ApiResponse<?> put(long id, ManualReportRequest manualReportRequest) {
        ManualReport manualReport = repo.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, String.format(MessageUtil.NOT_FOUND_BY_ID, id)));
        Optional.ofNullable(manualReportRequest.getCode()).ifPresent(manualReport::setCode);
        Optional.ofNullable(manualReportRequest.getShortName()).ifPresent(manualReport::setShortName);
        Optional.ofNullable(manualReportRequest.getNameUz()).ifPresent(manualReport::setNameUz);
        Optional.ofNullable(manualReportRequest.getNameUzCyril()).ifPresent(manualReport::setNameUzCyril);
        Optional.ofNullable(manualReportRequest.getNameRu()).ifPresent(manualReport::setNameRu);
        Optional.ofNullable(manualReportRequest.getIncludeInTotal()).ifPresent(manualReport::setIncludeInTotal);
        Optional.ofNullable(manualReportRequest.getNameKaa()).ifPresent(manualReport::setNameKaa);
        Optional.ofNullable(manualReportRequest.getMkb10Codes()).ifPresent(reqCodes -> {
            if (manualReport.getMkb10Codes() == null) {
                manualReport.setMkb10Codes(new HashSet<>());
            } else {
                manualReport.getMkb10Codes().clear();
            }

            manualReport.getMkb10Codes().addAll(reqCodes);
        });

        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, mapper.toResponse(repo.save(manualReport)));
    }
}

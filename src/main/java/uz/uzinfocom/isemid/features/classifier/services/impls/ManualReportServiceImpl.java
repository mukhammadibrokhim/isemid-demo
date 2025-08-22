package uz.uzinfocom.isemid.features.classifier.services.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.ManualReportRequest;
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
    public Page<ManualReport> filter(int page, int size, String shortName, String nameUz, String nameUzCyril, String nameRu, String nameKaa, String code, Boolean includeTotal) {
        Specification<ManualReport> spec = ManualReportSpecification.build(shortName, nameUz, nameUzCyril, nameRu, nameKaa, code, includeTotal);
        return repo.findAll(spec, pageableNotSorting(page, size));
    }

    @Override
    public ManualReport create(ManualReportRequest request) {
        ManualReport manualReport = mapper.toEntity(request);
        return repo.save(manualReport);
    }

    @Override
    public ManualReport getById(long id) {
        return repo.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Override
    public void delete(long id) {
        ManualReport report = repo.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
        try {
            repo.delete(report);
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(CONFLICT, messageUtil.get("error.cannot_delete_due_to_relations"));
        } catch (Exception ex) {
            throw new ApiException(INTERNAL_SERVER_ERROR, messageUtil.get("error.internal_server"), ex);
        }
    }

    @Override
    public ManualReport update(long id, ManualReportRequest request) {
        ManualReport manualReport = repo.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
        mapper.update(manualReport, request);
        return repo.save(manualReport);
    }
}

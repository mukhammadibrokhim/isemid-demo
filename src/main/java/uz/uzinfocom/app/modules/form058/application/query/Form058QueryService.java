package uz.uzinfocom.app.modules.form058.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form058.application.exception.Form058NotFoundException;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.application.query.dto.Form058DetailResult;
import uz.uzinfocom.app.modules.form058.application.query.dto.Form058TableResult;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058QueryRepository;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class Form058QueryService {

    private static final Map<String, String> ALLOWED_SORT_FIELDS = Map.of(
            "id", "id",
            "status", "status",
            "visitDate", "visitDate",
            "initialReportDateTime", "initialReportDateTime",
            "patientFullName", "patientFullName",
            "mkb10Code", "mkb10Code"
    );

    private final Form058QueryRepository form058QueryRepository;
    private final Form058QueryMapper form058QueryMapper;

    @Transactional(readOnly = true)
    public Page<Form058TableResult> findSent(Form058Filter filter) {
        return form058QueryRepository.findTable(normalize(filter), currentOrganizationId(), false, ALLOWED_SORT_FIELDS)
                .map(form058QueryMapper::toTableResult);
    }

    @Transactional(readOnly = true)
    public Page<Form058TableResult> findReceived(Form058Filter filter) {
        return form058QueryRepository.findTable(normalize(filter), currentOrganizationId(), true, ALLOWED_SORT_FIELDS)
                .map(form058QueryMapper::toTableResult);
    }

    @Transactional(readOnly = true)
    public Form058DetailResult getById(Long id) {
        Long organizationId = currentOrganizationId();
        return form058QueryRepository.findVisibleById(id, organizationId)
                .map(form058QueryMapper::toDetailResult)
                .orElseThrow(() -> new Form058NotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Form058DetailResult getByNnuzb(String nnuzb) {
        Long organizationId = currentOrganizationId();
        return form058QueryRepository.findLatestVisibleByNnuzb(nnuzb, organizationId)
                .map(form058QueryMapper::toDetailResult)
                .orElseThrow(() -> new Form058NotFoundException(nnuzb));
    }

    @Transactional(readOnly = true)
    public Form058DetailResult getByCard(Long cardId) {
        Long organizationId = currentOrganizationId();
        return form058QueryRepository.findVisibleByCard(cardId, organizationId)
                .map(form058QueryMapper::toDetailResult)
                .orElseThrow(() -> new Form058NotFoundException(cardId));
    }

    @Transactional(readOnly = true)
    public Map<FormStatus, Long> confirmationStats() {
        Long organizationId = currentOrganizationId();
        Map<FormStatus, Long> stats = new EnumMap<>(FormStatus.class);
        form058QueryRepository.countStatusesByOrganization(organizationId)
                .forEach(row -> stats.put(row.getStatus(), row.getCount()));
        return stats;
    }

    private Long currentOrganizationId() {
        return CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form058ScopeViolationException::new);
    }

    private Form058Filter normalize(Form058Filter filter) {
        return filter == null
                ? new Form058Filter(null, null, null, null, null, null, null, null)
                : filter;
    }
}

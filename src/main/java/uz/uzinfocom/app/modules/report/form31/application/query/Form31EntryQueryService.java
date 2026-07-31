package uz.uzinfocom.app.modules.report.form31.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form31.application.query.dto.Form31EntryFilterRequest;
import uz.uzinfocom.app.modules.report.form31.application.query.dto.Form31EntryTableResponse;
import uz.uzinfocom.app.modules.report.form31.application.query.mapper.Form31EntryMapper;
import uz.uzinfocom.app.modules.report.form31.domain.Form31Entry;
import uz.uzinfocom.app.modules.report.form31.infrastructure.persistence.repository.Form31EntryRepository;
import uz.uzinfocom.app.modules.report.form31.infrastructure.persistence.specification.Form31EntrySpecification;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Organization-scoped table listing for "Shakl №3-1" manual entries
 * (ILI/SARI sentinel surveillance + flu vaccination coverage). Every field
 * is operator-entered, so — unlike {@code Form2ManualEntryQueryService} —
 * there is no auto-computed snapshot to prefill; this class only handles
 * the scoped listing.
 */
@Service
@RequiredArgsConstructor
public class Form31EntryQueryService {

    private final Form31EntryRepository form31EntryRepository;
    private final Form31EntrySpecification form31EntrySpecification;
    private final Form31EntryMapper form31EntryMapper;
    private final OrganizationRepository organizationRepository;
    private final OrganizationScopeResolver organizationScopeResolver;

    @Transactional(readOnly = true)
    public Page<Form31EntryTableResponse> findTable(Form31EntryFilterRequest request) {
        Form31EntryFilterRequest filter = request == null
                ? new Form31EntryFilterRequest(null, null, null, null, null, null)
                : request;
        Pageable pageable = PageableUtils.of(filter, Form31EntrySortFields.ALLOWED_SORT_FIELDS);
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(requireCurrentOrganization());

        Page<Form31Entry> page = form31EntryRepository.findAll(
                form31EntrySpecification.byFilter(filter, scope), pageable
        );

        Map<Long, Organization> organizationsById = organizationRepository
                .findAllById(page.getContent().stream().map(Form31Entry::getOrganizationId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Organization::getId, org -> org));

        return page.map(entity -> form31EntryMapper.toTableResponse(
                entity, organizationsById.get(entity.getOrganizationId())
        ));
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}

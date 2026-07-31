package uz.uzinfocom.app.modules.report.form32.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form32.application.query.dto.Form32EntryFilterRequest;
import uz.uzinfocom.app.modules.report.form32.application.query.dto.Form32EntryTableResponse;
import uz.uzinfocom.app.modules.report.form32.application.query.mapper.Form32EntryMapper;
import uz.uzinfocom.app.modules.report.form32.domain.Form32Entry;
import uz.uzinfocom.app.modules.report.form32.infrastructure.persistence.repository.Form32EntryRepository;
import uz.uzinfocom.app.modules.report.form32.infrastructure.persistence.specification.Form32EntrySpecification;
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
 * Organization-scoped table listing for "Shakl №3-2" manual entries
 * (sanitary inspection activity counts). Every field is operator-entered,
 * so there is no auto-computed snapshot to prefill — mirrors {@code
 * Form31EntryQueryService}.
 */
@Service
@RequiredArgsConstructor
public class Form32EntryQueryService {

    private final Form32EntryRepository form32EntryRepository;
    private final Form32EntrySpecification form32EntrySpecification;
    private final Form32EntryMapper form32EntryMapper;
    private final OrganizationRepository organizationRepository;
    private final OrganizationScopeResolver organizationScopeResolver;

    @Transactional(readOnly = true)
    public Page<Form32EntryTableResponse> findTable(Form32EntryFilterRequest request) {
        Form32EntryFilterRequest filter = request == null
                ? new Form32EntryFilterRequest(null, null, null, null, null, null)
                : request;
        Pageable pageable = PageableUtils.of(filter, Form32EntrySortFields.ALLOWED_SORT_FIELDS);
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(requireCurrentOrganization());

        Page<Form32Entry> page = form32EntryRepository.findAll(
                form32EntrySpecification.byFilter(filter, scope), pageable
        );

        Map<Long, Organization> organizationsById = organizationRepository
                .findAllById(page.getContent().stream().map(Form32Entry::getOrganizationId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Organization::getId, org -> org));

        return page.map(entity -> form32EntryMapper.toTableResponse(
                entity, organizationsById.get(entity.getOrganizationId())
        ));
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}

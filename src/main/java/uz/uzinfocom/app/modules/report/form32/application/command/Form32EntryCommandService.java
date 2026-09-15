package uz.uzinfocom.app.modules.report.form32.application.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form32.application.command.dto.Form32EntryCreateRequest;
import uz.uzinfocom.app.modules.report.form32.application.command.dto.Form32EntryUpdateRequest;
import uz.uzinfocom.app.modules.report.form32.application.query.dto.Form32EntryTableResponse;
import uz.uzinfocom.app.modules.report.form32.application.query.mapper.Form32EntryMapper;
import uz.uzinfocom.app.modules.report.form32.domain.Form32Entry;
import uz.uzinfocom.app.modules.report.form32.infrastructure.persistence.repository.Form32EntryRepository;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

/**
 * Full CRUD for "Shakl №3-2" manual entries. Update and delete are gated by
 * {@link Form32EntryOwnershipValidator}: only the organization that created
 * an entry (or {@code isemid_admin}/{@code isemid_super_admin}) may change
 * or remove it — read access remains the wider organization-scope rule
 * enforced separately in {@code Form32EntryQueryService#findTable}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Form32EntryCommandService {

    private final Form32EntryRepository form32EntryRepository;
    private final Form32EntryMapper form32EntryMapper;
    private final Form32EntryOwnershipValidator form32EntryOwnershipValidator;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public Form32EntryTableResponse create(Form32EntryCreateRequest request) {
        Organization currentOrganization = requireCurrentOrganization();

        Form32Entry entry = Form32Entry.builder()
                .organizationId(currentOrganization.getId())
                .fromDate(request.from())
                .toDate(request.to())
                .inspectedTotalCount(nullToZero(request.inspectedTotalCount()))
                .inspectedMtmCount(nullToZero(request.inspectedMtmCount()))
                .inspectedSchoolCount(nullToZero(request.inspectedSchoolCount()))
                .inspectedDpmCount(nullToZero(request.inspectedDpmCount()))
                .inspectedOtherCount(nullToZero(request.inspectedOtherCount()))
                .deficiencyTotalCount(nullToZero(request.deficiencyTotalCount()))
                .deficiencyMtmCount(nullToZero(request.deficiencyMtmCount()))
                .deficiencySchoolCount(nullToZero(request.deficiencySchoolCount()))
                .deficiencyDpmCount(nullToZero(request.deficiencyDpmCount()))
                .deficiencyOtherCount(nullToZero(request.deficiencyOtherCount()))
                .officialTotalCount(nullToZero(request.officialTotalCount()))
                .officialMtmCount(nullToZero(request.officialMtmCount()))
                .officialSchoolCount(nullToZero(request.officialSchoolCount()))
                .officialDpmCount(nullToZero(request.officialDpmCount()))
                .officialOtherCount(nullToZero(request.officialOtherCount()))
                .suspendedTotalCount(nullToZero(request.suspendedTotalCount()))
                .suspendedMtmCount(nullToZero(request.suspendedMtmCount()))
                .suspendedSchoolCount(nullToZero(request.suspendedSchoolCount()))
                .suspendedDpmCount(nullToZero(request.suspendedDpmCount()))
                .suspendedOtherCount(nullToZero(request.suspendedOtherCount()))
                .build();

        Form32Entry saved = form32EntryRepository.save(entry);
        log.debug(
                "Shakl No3-2 entry created. id={}, organizationId={}, from={}, to={}",
                saved.getId(), saved.getOrganizationId(), saved.getFromDate(), saved.getToDate()
        );

        return form32EntryMapper.toTableResponse(saved, currentOrganization);
    }

    @Transactional
    public Form32EntryTableResponse update(Long id, Form32EntryUpdateRequest request) {
        Form32Entry entry = requireEntry(id);
        form32EntryOwnershipValidator.validate(entry);

        entry.setFromDate(request.from());
        entry.setToDate(request.to());
        entry.setInspectedTotalCount(nullToZero(request.inspectedTotalCount()));
        entry.setInspectedMtmCount(nullToZero(request.inspectedMtmCount()));
        entry.setInspectedSchoolCount(nullToZero(request.inspectedSchoolCount()));
        entry.setInspectedDpmCount(nullToZero(request.inspectedDpmCount()));
        entry.setInspectedOtherCount(nullToZero(request.inspectedOtherCount()));
        entry.setDeficiencyTotalCount(nullToZero(request.deficiencyTotalCount()));
        entry.setDeficiencyMtmCount(nullToZero(request.deficiencyMtmCount()));
        entry.setDeficiencySchoolCount(nullToZero(request.deficiencySchoolCount()));
        entry.setDeficiencyDpmCount(nullToZero(request.deficiencyDpmCount()));
        entry.setDeficiencyOtherCount(nullToZero(request.deficiencyOtherCount()));
        entry.setOfficialTotalCount(nullToZero(request.officialTotalCount()));
        entry.setOfficialMtmCount(nullToZero(request.officialMtmCount()));
        entry.setOfficialSchoolCount(nullToZero(request.officialSchoolCount()));
        entry.setOfficialDpmCount(nullToZero(request.officialDpmCount()));
        entry.setOfficialOtherCount(nullToZero(request.officialOtherCount()));
        entry.setSuspendedTotalCount(nullToZero(request.suspendedTotalCount()));
        entry.setSuspendedMtmCount(nullToZero(request.suspendedMtmCount()));
        entry.setSuspendedSchoolCount(nullToZero(request.suspendedSchoolCount()));
        entry.setSuspendedDpmCount(nullToZero(request.suspendedDpmCount()));
        entry.setSuspendedOtherCount(nullToZero(request.suspendedOtherCount()));

        Form32Entry saved = form32EntryRepository.save(entry);
        log.debug("Shakl No3-2 entry updated. id={}, organizationId={}", saved.getId(), saved.getOrganizationId());

        Organization owningOrganization = organizationRepository.findById(saved.getOrganizationId()).orElse(null);
        return form32EntryMapper.toTableResponse(saved, owningOrganization);
    }

    @Transactional
    public void delete(Long id) {
        Form32Entry entry = requireEntry(id);
        form32EntryOwnershipValidator.validate(entry);

        form32EntryRepository.delete(entry);
        log.debug("Shakl No3-2 entry deleted. id={}, organizationId={}", entry.getId(), entry.getOrganizationId());
    }

    private Form32Entry requireEntry(Long id) {
        return form32EntryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("report.form32_entry.not_found_by_id", id));
    }

    private Integer nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}

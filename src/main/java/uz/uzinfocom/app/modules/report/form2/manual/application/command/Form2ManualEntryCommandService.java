package uz.uzinfocom.app.modules.report.form2.manual.application.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form2.manual.application.command.dto.Form2ManualEntryCreateRequest;
import uz.uzinfocom.app.modules.report.form2.manual.application.command.dto.Form2ManualEntryUpdateRequest;
import uz.uzinfocom.app.modules.report.form2.manual.application.query.Form2ManualEntryQueryService;
import uz.uzinfocom.app.modules.report.form2.manual.application.query.dto.Form2ManualEntryTableResponse;
import uz.uzinfocom.app.modules.report.form2.manual.application.query.mapper.Form2ManualEntryMapper;
import uz.uzinfocom.app.modules.report.form2.manual.domain.Form2ManualEntry;
import uz.uzinfocom.app.modules.report.form2.manual.infrastructure.persistence.repository.Form2ManualEntryRepository;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

/**
 * Full CRUD for "Shakl №2" manual entries. Create/update always trust the
 * server, never the request, for the two auto-computed counts — {@link
 * Form2ManualEntryQueryService#registeredCaseCounts} is re-run from
 * {@code request.from()}/{@code request.to()} every time. Update and delete
 * are additionally gated by {@link Form2ManualEntryOwnershipValidator}: only
 * the organization that created an entry (or {@code isemid_admin}/{@code
 * isemid_super_admin}) may change or remove it — read access remains the
 * wider organization-scope rule enforced separately in {@code
 * Form2ManualEntryQueryService#findTable}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Form2ManualEntryCommandService {

    private final Form2ManualEntryRepository form2ManualEntryRepository;
    private final Form2ManualEntryQueryService form2ManualEntryQueryService;
    private final Form2ManualEntryMapper form2ManualEntryMapper;
    private final Form2ManualEntryOwnershipValidator form2ManualEntryOwnershipValidator;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public Form2ManualEntryTableResponse create(Form2ManualEntryCreateRequest request) {
        Organization currentOrganization = requireCurrentOrganization();

        Form2ManualEntryQueryService.RegisteredCaseCounts counts = form2ManualEntryQueryService
                .registeredCaseCounts(currentOrganization.getId(), request.from(), request.to());

        Form2ManualEntry entry = Form2ManualEntry.builder()
                .organizationId(currentOrganization.getId())
                .fromDate(request.from())
                .toDate(request.to())
                .registeredCasesLastYear(counts.lastYear())
                .registeredCasesCurrentYear(counts.currentYear())
                .mtmSchoolCount(nullToZero(request.mtmSchoolCount()))
                .disinfectionFociCount(nullToZero(request.disinfectionFociCount()))
                .inspectedObjectsCount(nullToZero(request.inspectedObjectsCount()))
                .closedObjectsCount(nullToZero(request.closedObjectsCount()))
                .dduCount(nullToZero(request.dduCount()))
                .schoolCount(nullToZero(request.schoolCount()))
                .finesCount(nullToZero(request.finesCount()))
                .prosecutorReferralCount(nullToZero(request.prosecutorReferralCount()))
                .build();

        Form2ManualEntry saved = form2ManualEntryRepository.save(entry);
        log.debug(
                "Shakl No2 manual entry created. id={}, organizationId={}, from={}, to={}",
                saved.getId(), saved.getOrganizationId(), saved.getFromDate(), saved.getToDate()
        );

        return form2ManualEntryMapper.toTableResponse(saved, currentOrganization);
    }

    @Transactional
    public Form2ManualEntryTableResponse update(Long id, Form2ManualEntryUpdateRequest request) {
        Form2ManualEntry entry = requireEntry(id);
        form2ManualEntryOwnershipValidator.validate(entry);

        Form2ManualEntryQueryService.RegisteredCaseCounts counts = form2ManualEntryQueryService
                .registeredCaseCounts(entry.getOrganizationId(), request.from(), request.to());

        entry.setFromDate(request.from());
        entry.setToDate(request.to());
        entry.setRegisteredCasesLastYear(counts.lastYear());
        entry.setRegisteredCasesCurrentYear(counts.currentYear());
        entry.setMtmSchoolCount(nullToZero(request.mtmSchoolCount()));
        entry.setDisinfectionFociCount(nullToZero(request.disinfectionFociCount()));
        entry.setInspectedObjectsCount(nullToZero(request.inspectedObjectsCount()));
        entry.setClosedObjectsCount(nullToZero(request.closedObjectsCount()));
        entry.setDduCount(nullToZero(request.dduCount()));
        entry.setSchoolCount(nullToZero(request.schoolCount()));
        entry.setFinesCount(nullToZero(request.finesCount()));
        entry.setProsecutorReferralCount(nullToZero(request.prosecutorReferralCount()));

        Form2ManualEntry saved = form2ManualEntryRepository.save(entry);
        log.debug("Shakl No2 manual entry updated. id={}, organizationId={}", saved.getId(), saved.getOrganizationId());

        Organization owningOrganization = organizationRepository.findById(saved.getOrganizationId()).orElse(null);
        return form2ManualEntryMapper.toTableResponse(saved, owningOrganization);
    }

    @Transactional
    public void delete(Long id) {
        Form2ManualEntry entry = requireEntry(id);
        form2ManualEntryOwnershipValidator.validate(entry);

        form2ManualEntryRepository.delete(entry);
        log.debug("Shakl No2 manual entry deleted. id={}, organizationId={}", entry.getId(), entry.getOrganizationId());
    }

    private Form2ManualEntry requireEntry(Long id) {
        return form2ManualEntryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("report.form2_manual_entry.not_found_by_id", id));
    }

    private Integer nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}

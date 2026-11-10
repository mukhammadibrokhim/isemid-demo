package uz.uzinfocom.app.modules.report.form7.application.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.modules.report.form7.application.command.dto.Form7EntryCreateRequest;
import uz.uzinfocom.app.modules.report.form7.application.command.dto.Form7EntryUpdateRequest;
import uz.uzinfocom.app.modules.report.form7.application.query.Form7EntryQueryService;
import uz.uzinfocom.app.modules.report.form7.application.query.dto.Form7CaseCountProjection;
import uz.uzinfocom.app.modules.report.form7.application.query.dto.Form7EntryTableResponse;
import uz.uzinfocom.app.modules.report.form7.application.query.mapper.Form7EntryMapper;
import uz.uzinfocom.app.modules.report.form7.domain.Form7Entry;
import uz.uzinfocom.app.modules.report.form7.infrastructure.persistence.repository.Form7EntryRepository;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

/**
 * Full CRUD for "Shakl №7" manual entries. Create/update always trust the
 * server, never the request, for the auto-computed block — {@link
 * Form7EntryQueryService#caseCounts} is re-run from {@code request.from()}/
 * {@code request.to()} every time. Update and delete are additionally gated
 * by {@link Form7EntryOwnershipValidator}: only the organization that created
 * an entry (or {@code isemid_admin}/{@code isemid_super_admin}) may change or
 * remove it — read access remains the wider organization-scope rule enforced
 * separately in {@code Form7EntryQueryService#findTable}. Mirrors
 * {@code Form2ManualEntryCommandService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Form7EntryCommandService {

    private final Form7EntryRepository form7EntryRepository;
    private final Form7EntryQueryService form7EntryQueryService;
    private final Form7EntryMapper form7EntryMapper;
    private final Form7EntryOwnershipValidator form7EntryOwnershipValidator;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public Form7EntryTableResponse create(Form7EntryCreateRequest request) {
        Organization currentOrganization = requireCurrentOrganization();

        Form7CaseCountProjection counts = form7EntryQueryService
                .caseCounts(currentOrganization.getId(), request.from(), request.to());

        Form7Entry entry = Form7Entry.builder()
                .organizationId(currentOrganization.getId())
                .fromDate(request.from())
                .toDate(request.to())
                .casesAtPeriodStart(nullToZero(request.casesAtPeriodStart()))
                .registeredTotal(counts.total())
                .registeredUnder14(counts.under14())
                .registeredUnder18(counts.under18())
                .registeredAdult(counts.adult())
                .registeredFemale(counts.female())
                .registeredUrbanCount(nullToZero(request.registeredUrbanCount()))
                .registeredRuralCount(nullToZero(request.registeredRuralCount()))
                .examinedCount(nullToZero(request.examinedCount()))
                .toBeExaminedCount(nullToZero(request.toBeExaminedCount()))
                .primaryDiagnosisConfirmed(counts.primaryDiagnosisConfirmed())
                .hospitalizedCount(nullToZero(request.hospitalizedCount()))
                .casesAtPeriodEnd(nullToZero(request.casesAtPeriodEnd()))
                .build();

        Form7Entry saved = form7EntryRepository.save(entry);
        log.debug(
                "Shakl No7 entry created. id={}, organizationId={}, from={}, to={}",
                saved.getId(), saved.getOrganizationId(), saved.getFromDate(), saved.getToDate()
        );

        return form7EntryMapper.toTableResponse(saved, currentOrganization);
    }

    @Transactional
    public Form7EntryTableResponse update(Long id, Form7EntryUpdateRequest request) {
        Form7Entry entry = requireEntry(id);
        form7EntryOwnershipValidator.validate(entry);

        Form7CaseCountProjection counts = form7EntryQueryService
                .caseCounts(entry.getOrganizationId(), request.from(), request.to());

        entry.setFromDate(request.from());
        entry.setToDate(request.to());
        entry.setCasesAtPeriodStart(nullToZero(request.casesAtPeriodStart()));
        entry.setRegisteredTotal(counts.total());
        entry.setRegisteredUnder14(counts.under14());
        entry.setRegisteredUnder18(counts.under18());
        entry.setRegisteredAdult(counts.adult());
        entry.setRegisteredFemale(counts.female());
        entry.setRegisteredUrbanCount(nullToZero(request.registeredUrbanCount()));
        entry.setRegisteredRuralCount(nullToZero(request.registeredRuralCount()));
        entry.setExaminedCount(nullToZero(request.examinedCount()));
        entry.setToBeExaminedCount(nullToZero(request.toBeExaminedCount()));
        entry.setPrimaryDiagnosisConfirmed(counts.primaryDiagnosisConfirmed());
        entry.setHospitalizedCount(nullToZero(request.hospitalizedCount()));
        entry.setCasesAtPeriodEnd(nullToZero(request.casesAtPeriodEnd()));

        Form7Entry saved = form7EntryRepository.save(entry);
        log.debug("Shakl No7 entry updated. id={}, organizationId={}", saved.getId(), saved.getOrganizationId());

        Organization owningOrganization = organizationRepository.findById(saved.getOrganizationId()).orElse(null);
        return form7EntryMapper.toTableResponse(saved, owningOrganization);
    }

    @Transactional
    public void delete(Long id) {
        Form7Entry entry = requireEntry(id);
        form7EntryOwnershipValidator.validate(entry);

        form7EntryRepository.delete(entry);
        log.debug("Shakl No7 entry deleted. id={}, organizationId={}", entry.getId(), entry.getOrganizationId());
    }

    private Form7Entry requireEntry(Long id) {
        return form7EntryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("report.form7_entry.not_found_by_id", id));
    }

    private Integer nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}

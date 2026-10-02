package uz.uzinfocom.app.modules.report.form31.application.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.report.form31.application.command.dto.Form31EntryCreateRequest;
import uz.uzinfocom.app.modules.report.form31.application.command.dto.Form31EntryUpdateRequest;
import uz.uzinfocom.app.modules.report.form31.application.query.dto.Form31EntryTableResponse;
import uz.uzinfocom.app.modules.report.form31.application.query.mapper.Form31EntryMapper;
import uz.uzinfocom.app.modules.report.form31.domain.Form31Entry;
import uz.uzinfocom.app.modules.report.form31.infrastructure.persistence.repository.Form31EntryRepository;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

/**
 * Full CRUD for "Shakl №3-1" manual entries. Update and delete are gated by
 * {@link Form31EntryOwnershipValidator}: only the organization that created
 * an entry (or {@code isemid_admin}/{@code isemid_super_admin}) may change
 * or remove it — read access remains the wider organization-scope rule
 * enforced separately in {@code Form31EntryQueryService#findTable}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Form31EntryCommandService {

    private final Form31EntryRepository form31EntryRepository;
    private final Form31EntryMapper form31EntryMapper;
    private final Form31EntryOwnershipValidator form31EntryOwnershipValidator;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public Form31EntryTableResponse create(Form31EntryCreateRequest request) {
        Organization currentOrganization = requireCurrentOrganization();

        Form31Entry entry = Form31Entry.builder()
                .organizationId(currentOrganization.getId())
                .fromDate(request.from())
                .toDate(request.to())
                .iliCasesCount(nullToZero(request.iliCasesCount()))
                .ariCasesCount(nullToZero(request.ariCasesCount()))
                .pneumoniaCasesCount(nullToZero(request.pneumoniaCasesCount()))
                .sariTotalCount(nullToZero(request.sariTotalCount()))
                .sariPregnantCount(nullToZero(request.sariPregnantCount()))
                .deathTotalCount(nullToZero(request.deathTotalCount()))
                .deathPregnantCount(nullToZero(request.deathPregnantCount()))
                .weeklyVaccinationCount(nullToZero(request.weeklyVaccinationCount()))
                .seasonVaccinationCount(nullToZero(request.seasonVaccinationCount()))
                .build();

        Form31Entry saved = form31EntryRepository.save(entry);
        log.debug(
                "Shakl No3-1 entry created. id={}, organizationId={}, from={}, to={}",
                saved.getId(), saved.getOrganizationId(), saved.getFromDate(), saved.getToDate()
        );

        return form31EntryMapper.toTableResponse(saved, currentOrganization);
    }

    @Transactional
    public Form31EntryTableResponse update(Long id, Form31EntryUpdateRequest request) {
        Form31Entry entry = requireEntry(id);
        form31EntryOwnershipValidator.validate(entry);

        entry.setFromDate(request.from());
        entry.setToDate(request.to());
        entry.setIliCasesCount(nullToZero(request.iliCasesCount()));
        entry.setAriCasesCount(nullToZero(request.ariCasesCount()));
        entry.setPneumoniaCasesCount(nullToZero(request.pneumoniaCasesCount()));
        entry.setSariTotalCount(nullToZero(request.sariTotalCount()));
        entry.setSariPregnantCount(nullToZero(request.sariPregnantCount()));
        entry.setDeathTotalCount(nullToZero(request.deathTotalCount()));
        entry.setDeathPregnantCount(nullToZero(request.deathPregnantCount()));
        entry.setWeeklyVaccinationCount(nullToZero(request.weeklyVaccinationCount()));
        entry.setSeasonVaccinationCount(nullToZero(request.seasonVaccinationCount()));

        Form31Entry saved = form31EntryRepository.save(entry);
        log.debug("Shakl No3-1 entry updated. id={}, organizationId={}", saved.getId(), saved.getOrganizationId());

        Organization owningOrganization = organizationRepository.findById(saved.getOrganizationId()).orElse(null);
        return form31EntryMapper.toTableResponse(saved, owningOrganization);
    }

    @Transactional
    public void delete(Long id) {
        Form31Entry entry = requireEntry(id);
        form31EntryOwnershipValidator.validate(entry);

        form31EntryRepository.delete(entry);
        log.debug("Shakl No3-1 entry deleted. id={}, organizationId={}", entry.getId(), entry.getOrganizationId());
    }

    private Form31Entry requireEntry(Long id) {
        return form31EntryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("report.form31_entry.not_found_by_id", id));
    }

    private Integer nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    private Organization requireCurrentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
    }
}

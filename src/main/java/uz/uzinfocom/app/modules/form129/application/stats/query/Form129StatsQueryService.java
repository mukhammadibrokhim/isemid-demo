package uz.uzinfocom.app.modules.form129.application.stats.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form129.application.exception.Form129ScopeViolationException;
import uz.uzinfocom.app.modules.form129.application.stats.query.dto.Form129DailyCountResponse;
import uz.uzinfocom.app.modules.form129.application.stats.query.dto.Form129DiseaseTypeCountResponse;
import uz.uzinfocom.app.modules.form129.application.stats.query.dto.Form129MonthlyOutcomeCountResponse;
import uz.uzinfocom.app.modules.form129.application.stats.query.dto.Form129OrganizationCountResponse;
import uz.uzinfocom.app.modules.form129.application.stats.query.dto.Form129SourceCountResponse;
import uz.uzinfocom.app.modules.form129.application.stats.query.dto.Form129StatusCountResponse;
import uz.uzinfocom.app.modules.form129.infrastructure.persistence.repository.Form129StatsRepository;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.time.LocalDate;
import java.util.List;

/**
 * Stats source for the form129 home-dashboard tab. Unlike
 * {@code Form0581StatsQueryService}, there is no direction parameter here:
 * Form129 has sender/receiver like Form0581, but this dashboard layer always
 * reads the receiving SES organization's inbox — the same convention the
 * Form058/Form0581 dashboards use ({@code Form0581Direction.INCOMING}) —
 * while the table-A report layer ({@code modules.report.statistics}) reads
 * the sender side instead.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class Form129StatsQueryService {

    private final Form129StatsRepository form129StatsRepository;
    private final OrganizationScopeResolver organizationScopeResolver;

    public long countTotal() {
        return form129StatsRepository.countTotal(currentScope());
    }

    public long countActive() {
        return form129StatsRepository.countActive(currentScope());
    }

    public List<Form129DailyCountResponse> countByDay(LocalDate from, LocalDate to) {
        return form129StatsRepository.countByDay(currentScope(), from, to);
    }

    /** Bucketed by calendar month with a CANCELED/ACCEPTED breakdown — for the dashboard's dynamics chart. */
    public List<Form129MonthlyOutcomeCountResponse> countByMonthWithOutcomes(LocalDate from, LocalDate to) {
        return form129StatsRepository.countByMonthWithOutcomes(currentScope(), from, to);
    }

    public List<Form129StatusCountResponse> countByStatus() {
        return form129StatsRepository.countByStatus(currentScope());
    }

    public List<Form129DiseaseTypeCountResponse> countByDiseaseType() {
        return form129StatsRepository.countByDiseaseType(currentScope());
    }

    public List<Form129SourceCountResponse> countBySource() {
        return form129StatsRepository.countBySource(currentScope());
    }

    /**
     * Restricted-id variant — safe for non-admin callers because the caller
     * supplies the exact organization ids to aggregate (already resolved
     * from a legitimate scope elsewhere, e.g. the home dashboard's
     * region/district breakdown), for geographic breakdown.
     */
    public List<Form129OrganizationCountResponse> countByReceiverOrganizationWithinIds(List<Long> organizationIds) {
        return form129StatsRepository.countByReceiverOrganizationWithinIds(organizationIds);
    }

    private ResolvedOrganizationScope currentScope() {
        Organization currentOrganization = CurrentOrganizationContext.getOptional()
                .orElseThrow(Form129ScopeViolationException::new);

        return organizationScopeResolver.resolve(currentOrganization);
    }
}

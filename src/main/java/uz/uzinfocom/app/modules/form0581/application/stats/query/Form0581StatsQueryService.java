package uz.uzinfocom.app.modules.form0581.application.stats.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581ScopeViolationException;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581DailyCountResponse;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581Mkb10CountResponse;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581OrganizationCountResponse;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581SourceCountResponse;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581StatusCountResponse;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581StatsRepository;
import uz.uzinfocom.app.modules.form0581.web.dto.request.enums.Form0581Direction;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.authorization.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class Form0581StatsQueryService {

    private final Form0581StatsRepository form0581StatsRepository;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final AdminAccessGuard adminAccessGuard;

    public List<Form0581StatusCountResponse> countByStatus(Form0581Direction direction) {
        return form0581StatsRepository.countByStatus(currentScope(), receivedFor(direction));
    }

    public List<Form0581DailyCountResponse> countByDay(Form0581Direction direction, LocalDate from, LocalDate to) {
        return form0581StatsRepository.countByDay(currentScope(), receivedFor(direction), from, to);
    }

    public List<Form0581Mkb10CountResponse> topMkb10(Form0581Direction direction, int limit) {
        return form0581StatsRepository.topMkb10(currentScope(), receivedFor(direction), limit);
    }

    public List<Form0581SourceCountResponse> countBySource(Form0581Direction direction) {
        return form0581StatsRepository.countBySource(currentScope(), receivedFor(direction));
    }

    /**
     * Same as {@link #countByDay}, bucketed by calendar month — for
     * multi-month trend charts (e.g. the home dashboard).
     */
    public List<Form0581DailyCountResponse> countByMonth(Form0581Direction direction, LocalDate from, LocalDate to) {
        return form0581StatsRepository.countByMonth(currentScope(), receivedFor(direction), from, to);
    }

    /**
     * Restricted-id variant of {@link #countByReceiverOrganization} — safe for
     * non-admin callers because, unlike the admin-only unscoped variant, the
     * caller supplies the exact organization ids to aggregate (already
     * resolved from a legitimate scope elsewhere, e.g. the home dashboard's
     * region/district breakdown), so no organization's data beyond what was
     * explicitly requested can leak.
     */
    public List<Form0581OrganizationCountResponse> countByReceiverOrganizationWithinIds(List<Long> organizationIds) {
        return form0581StatsRepository.countByReceiverOrganizationWithinIds(organizationIds);
    }

    /**
     * Admin dashboard — unscoped (all organizations), unlike {@link #countByStatus}.
     */
    public List<Form0581StatusCountResponse> adminCountByStatus() {
        adminAccessGuard.requireAdmin();
        return form0581StatsRepository.countByStatusUnscoped();
    }

    public List<Form0581DailyCountResponse> adminCountByDay(LocalDate from, LocalDate to) {
        adminAccessGuard.requireAdmin();
        return form0581StatsRepository.countByDayUnscoped(from, to);
    }

    public List<Form0581Mkb10CountResponse> adminTopMkb10(int limit) {
        adminAccessGuard.requireAdmin();
        return form0581StatsRepository.topMkb10Unscoped(limit);
    }

    /**
     * Cross-organization breakdown — admin-only, since it shows every
     * organization's counts regardless of the caller's own scope.
     */
    public List<Form0581OrganizationCountResponse> countBySenderOrganization() {
        adminAccessGuard.requireAdmin();
        return form0581StatsRepository.countBySenderOrganization();
    }

    public List<Form0581OrganizationCountResponse> countByReceiverOrganization() {
        adminAccessGuard.requireAdmin();
        return form0581StatsRepository.countByReceiverOrganization();
    }

    private Boolean receivedFor(Form0581Direction direction) {
        return switch (direction) {
            case OUTGOING -> false;
            case INCOMING -> true;
            case ALL -> {
                adminAccessGuard.requireSuperAdmin();
                yield null;
            }
        };
    }

    private ResolvedOrganizationScope currentScope() {
        Organization currentOrganization = CurrentOrganizationContext.getOptional()
                .orElseThrow(Form0581ScopeViolationException::new);

        return organizationScopeResolver.resolve(currentOrganization);
    }
}

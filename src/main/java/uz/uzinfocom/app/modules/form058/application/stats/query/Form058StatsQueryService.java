package uz.uzinfocom.app.modules.form058.application.stats.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058DailyCountResponse;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058Mkb10CountResponse;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058OrganizationCountResponse;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058SourceCountResponse;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058StatusCountResponse;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058StatsRepository;
import uz.uzinfocom.app.modules.form058.web.dto.request.enums.Form058Direction;
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
public class Form058StatsQueryService {

    private final Form058StatsRepository form058StatsRepository;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final AdminAccessGuard adminAccessGuard;

    public List<Form058StatusCountResponse> countByStatus(Form058Direction direction) {
        return form058StatsRepository.countByStatus(currentScope(), receivedFor(direction));
    }

    public List<Form058DailyCountResponse> countByDay(Form058Direction direction, LocalDate from, LocalDate to) {
        return form058StatsRepository.countByDay(currentScope(), receivedFor(direction), from, to);
    }

    public List<Form058Mkb10CountResponse> topMkb10(Form058Direction direction, int limit) {
        return form058StatsRepository.topMkb10(currentScope(), receivedFor(direction), limit);
    }

    public List<Form058SourceCountResponse> countBySource(Form058Direction direction) {
        return form058StatsRepository.countBySource(currentScope(), receivedFor(direction));
    }

    /**
     * Same as {@link #countByDay}, bucketed by calendar month — for
     * multi-month trend charts (e.g. the home dashboard).
     */
    public List<Form058DailyCountResponse> countByMonth(Form058Direction direction, LocalDate from, LocalDate to) {
        return form058StatsRepository.countByMonth(currentScope(), receivedFor(direction), from, to);
    }

    /**
     * Restricted-id variant of {@link #countByReceiverOrganization} — safe for
     * non-admin callers because, unlike the admin-only unscoped variant, the
     * caller supplies the exact organization ids to aggregate (already
     * resolved from a legitimate scope elsewhere, e.g. the home dashboard's
     * region/district breakdown), so no organization's data beyond what was
     * explicitly requested can leak.
     */
    public List<Form058OrganizationCountResponse> countByReceiverOrganizationWithinIds(List<Long> organizationIds) {
        return form058StatsRepository.countByReceiverOrganizationWithinIds(organizationIds);
    }

    /**
     * Admin dashboard — unscoped (all organizations), unlike {@link #countByStatus}.
     */
    public List<Form058StatusCountResponse> adminCountByStatus() {
        adminAccessGuard.requireAdmin();
        return form058StatsRepository.countByStatusUnscoped();
    }

    public List<Form058DailyCountResponse> adminCountByDay(LocalDate from, LocalDate to) {
        adminAccessGuard.requireAdmin();
        return form058StatsRepository.countByDayUnscoped(from, to);
    }

    public List<Form058Mkb10CountResponse> adminTopMkb10(int limit) {
        adminAccessGuard.requireAdmin();
        return form058StatsRepository.topMkb10Unscoped(limit);
    }

    /**
     * Cross-organization breakdown — admin-only, since it shows every
     * organization's counts regardless of the caller's own scope.
     */
    public List<Form058OrganizationCountResponse> countBySenderOrganization() {
        adminAccessGuard.requireAdmin();
        return form058StatsRepository.countBySenderOrganization();
    }

    public List<Form058OrganizationCountResponse> countByReceiverOrganization() {
        adminAccessGuard.requireAdmin();
        return form058StatsRepository.countByReceiverOrganization();
    }

    private Boolean receivedFor(Form058Direction direction) {
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
                .orElseThrow(Form058ScopeViolationException::new);

        return organizationScopeResolver.resolve(currentOrganization);
    }
}

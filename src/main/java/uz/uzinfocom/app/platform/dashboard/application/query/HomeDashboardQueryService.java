package uz.uzinfocom.app.platform.dashboard.application.query;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.DashboardScopeResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.HomeDashboardResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.LevelTypeCountResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.MedicalInstitutionsResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.MedicalTypeCountResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.RoleCountResponse;
import uz.uzinfocom.app.platform.dashboard.application.query.dto.UsersResponse;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.iam.repository.UserStatsRepository;
import uz.uzinfocom.app.platform.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.platform.scope.OrganizationScopeMode;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.OrganizationScopeOrganizationIdResolver;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * The general ("everyone") home dashboard — a lightweight overview: medical
 * institutions and users in scope, nothing case/module-specific. Every
 * module's own statistics (form058, form058-1, card, act, ...) live behind
 * their own {@code /v1/dashboard/home/{module}} endpoint instead ({@link
 * Form058DashboardQueryService}, {@link Form0581DashboardQueryService},
 * {@link CardDashboardQueryService}, {@link ActDashboardQueryService}) - this
 * endpoint intentionally never computes any of that, so it stays cheap
 * enough to load unconditionally on every dashboard visit regardless of
 * which module the caller actually cares about.
 */
@Service
public class HomeDashboardQueryService {

    private final OrganizationScopeResolver organizationScopeResolver;
    private final OrganizationScopeOrganizationIdResolver organizationScopeOrganizationIdResolver;
    private final OrganizationRepository organizationRepository;
    private final UserStatsRepository userStatsRepository;
    private final ReferenceLookupService referenceLookupService;
    private final Executor applicationTaskExecutor;

    /*
     * Explicit constructor (not @RequiredArgsConstructor) solely so
     * @Qualifier can land on the executor constructor parameter - Lombok
     * does not copy arbitrary field annotations onto generated constructor
     * parameters without a project-wide lombok.config entry, and this is
     * the only field in the class that needs one.
     */
    public HomeDashboardQueryService(
            OrganizationScopeResolver organizationScopeResolver,
            OrganizationScopeOrganizationIdResolver organizationScopeOrganizationIdResolver,
            OrganizationRepository organizationRepository,
            UserStatsRepository userStatsRepository,
            ReferenceLookupService referenceLookupService,
            @Qualifier("applicationTaskExecutor") Executor applicationTaskExecutor
    ) {
        this.organizationScopeResolver = organizationScopeResolver;
        this.organizationScopeOrganizationIdResolver = organizationScopeOrganizationIdResolver;
        this.organizationRepository = organizationRepository;
        this.userStatsRepository = userStatsRepository;
        this.referenceLookupService = referenceLookupService;
        this.applicationTaskExecutor = applicationTaskExecutor;
    }

    public HomeDashboardResponse getHome() {
        Instant generatedAt = Instant.now();
        Organization currentOrganization = CurrentOrganizationContext.getOptional()
                .orElseThrow(() -> new ScopeViolationException("organization.scope_violation"));
        ResolvedOrganizationScope scope = organizationScopeResolver.resolve(currentOrganization);
        LocaleContext locale = LocaleContextHolder.getLocaleContext();

        CompletableFuture<DashboardScopeResponse> scopeFuture =
                supplyOrgScoped(currentOrganization, locale, () -> buildScope(scope));
        CompletableFuture<MedicalInstitutionsResponse> institutionsFuture =
                supplyOrgScoped(currentOrganization, locale, () -> buildMedicalInstitutions(scope));
        CompletableFuture<UsersResponse> usersFuture =
                supplyOrgScoped(currentOrganization, locale, () -> buildUsers(scope));

        CompletableFuture.allOf(scopeFuture, institutionsFuture, usersFuture).join();

        return new HomeDashboardResponse(
                generatedAt,
                scopeFuture.join(),
                institutionsFuture.join(),
                usersFuture.join()
        );
    }

    /**
     * Runs {@code supplier} on {@link #applicationTaskExecutor}, copying
     * both {@link CurrentOrganizationContext} and the request's {@link
     * LocaleContext} onto that worker thread first — both are ThreadLocal
     * and neither propagates to a new thread on its own. See {@code
     * Form058DashboardQueryService#supplyOrgScoped} for the full rationale
     * (organization scope resolution + locale-dependent name resolution).
     */
    private <T> CompletableFuture<T> supplyOrgScoped(Organization organization, LocaleContext locale, Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            CurrentOrganizationContext.set(organization);
            LocaleContextHolder.setLocaleContext(locale);
            try {
                return supplier.get();
            } finally {
                CurrentOrganizationContext.clear();
                LocaleContextHolder.resetLocaleContext();
            }
        }, applicationTaskExecutor);
    }

    private DashboardScopeResponse buildScope(ResolvedOrganizationScope scope) {
        String regionName = scope.regionCode() != null ? referenceLookupService.getRegionName(scope.regionCode()) : null;
        String districtName = scope.districtCode() != null
                ? referenceLookupService.getDistrictName(scope.districtCode())
                : null;

        return new DashboardScopeResponse(scope.mode(), scope.regionCode(), regionName, scope.districtCode(), districtName);
    }

    private MedicalInstitutionsResponse buildMedicalInstitutions(ResolvedOrganizationScope scope) {
        return new MedicalInstitutionsResponse(
                countMedicalInstitutions(scope),
                buildMedicalTypeBreakdown(scope),
                buildLevelTypeBreakdown(scope)
        );
    }

    private long countMedicalInstitutions(ResolvedOrganizationScope scope) {
        return switch (scope.mode()) {
            case ALL -> organizationRepository.countByActiveTrue();
            case REGION -> (long) organizationScopeOrganizationIdResolver.resolveScopeOrganizationIds(
                    OrganizationScopeMode.REGION, scope.regionCode(), scope.districtCode()
            ).size();
            case DISTRICT -> (long) organizationScopeOrganizationIdResolver.resolveScopeOrganizationIds(
                    OrganizationScopeMode.DISTRICT, scope.regionCode(), scope.districtCode()
            ).size();
            case ORGANIZATION -> 1L;
        };
    }

    /**
     * ORGANIZATION mode never queries the database for this - a single
     * organization's own medicalType is already known from {@link
     * ResolvedOrganizationScope} itself, resolved once when the caller's
     * scope was computed.
     */
    private List<MedicalTypeCountResponse> buildMedicalTypeBreakdown(ResolvedOrganizationScope scope) {
        return switch (scope.mode()) {
            case ALL -> organizationRepository.countActiveByMedicalType(null, null).stream()
                    .map(item -> new MedicalTypeCountResponse(item.medicalType(), item.count()))
                    .toList();
            case REGION -> organizationRepository.countActiveByMedicalType(scope.regionCode(), null).stream()
                    .map(item -> new MedicalTypeCountResponse(item.medicalType(), item.count()))
                    .toList();
            case DISTRICT -> organizationRepository.countActiveByMedicalType(null, scope.districtCode()).stream()
                    .map(item -> new MedicalTypeCountResponse(item.medicalType(), item.count()))
                    .toList();
            case ORGANIZATION -> List.of(new MedicalTypeCountResponse(scope.medicalType(), 1L));
        };
    }

    /** Same scope rules as {@link #buildMedicalTypeBreakdown}, grouped by levelType instead. */
    private List<LevelTypeCountResponse> buildLevelTypeBreakdown(ResolvedOrganizationScope scope) {
        return switch (scope.mode()) {
            case ALL -> organizationRepository.countActiveByLevelType(null, null).stream()
                    .map(item -> new LevelTypeCountResponse(item.levelType(), item.count()))
                    .toList();
            case REGION -> organizationRepository.countActiveByLevelType(scope.regionCode(), null).stream()
                    .map(item -> new LevelTypeCountResponse(item.levelType(), item.count()))
                    .toList();
            case DISTRICT -> organizationRepository.countActiveByLevelType(null, scope.districtCode()).stream()
                    .map(item -> new LevelTypeCountResponse(item.levelType(), item.count()))
                    .toList();
            case ORGANIZATION -> List.of(new LevelTypeCountResponse(scope.levelType(), 1L));
        };
    }

    private UsersResponse buildUsers(ResolvedOrganizationScope scope) {
        long total = userStatsRepository.countTotal(scope);
        List<RoleCountResponse> byRole = userStatsRepository.countByRole(scope).stream()
                .map(item -> new RoleCountResponse(item.roleName(), item.count()))
                .toList();

        return new UsersResponse(total, byRole);
    }
}

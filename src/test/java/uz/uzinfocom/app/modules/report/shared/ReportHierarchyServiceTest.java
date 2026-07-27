package uz.uzinfocom.app.modules.report.shared;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationGeoProjection;
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationNameResolver;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.platform.reference.domain.Region;
import uz.uzinfocom.app.platform.reference.repository.DistrictRepository;
import uz.uzinfocom.app.platform.reference.repository.RegionRepository;
import uz.uzinfocom.app.platform.scope.OrganizationScopeMode;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.OrganizationScopeOrganizationIdResolver;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Focused unit test for the hierarchy walker shared by every report under
 * {@code modules.report} ("Shakl №3-1", "Shakl №2", "Shakl №3"). Uses a
 * trivial {@code ReportCountSource<Long>} fake instead of any real report's
 * SQL, since this class's job is scope/geography dispatch, not counting.
 */
class ReportHierarchyServiceTest {

    private final OrganizationScopeResolver organizationScopeResolver = mock(OrganizationScopeResolver.class);
    private final OrganizationScopeOrganizationIdResolver organizationScopeOrganizationIdResolver =
            mock(OrganizationScopeOrganizationIdResolver.class);
    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
    private final DistrictRepository districtRepository = mock(DistrictRepository.class);
    private final RegionRepository regionRepository = mock(RegionRepository.class);
    private final ReferenceLookupService referenceLookupService = mock(ReferenceLookupService.class);
    private final OrganizationNameResolver organizationNameResolver = mock(OrganizationNameResolver.class);
    private final MessageResolver messageResolver = mock(MessageResolver.class);

    private final ReportHierarchyService reportHierarchyService = new ReportHierarchyService(
            organizationScopeResolver,
            organizationScopeOrganizationIdResolver,
            organizationRepository,
            districtRepository,
            regionRepository,
            referenceLookupService,
            organizationNameResolver,
            messageResolver
    );

    private final ReportDateRange range = new ReportDateRange(Instant.EPOCH, Instant.EPOCH.plusSeconds(86_400));

    private static final ReportCountSource<Long> LONG_COUNT_SOURCE = new ReportCountSource<>() {
        @Override
        public Long total(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
            return organizationIds.size() * 10L;
        }

        @Override
        public Map<Long, Long> groupedByOrganization(List<Long> organizationIds, ReportDateRange range, String diagnosisCode) {
            return Map.of(10L, 5L, 20L, 7L);
        }

        @Override
        public Long empty() {
            return 0L;
        }

        @Override
        public Long merge(Long a, Long b) {
            return a + b;
        }
    };

    private Organization organization(MedicalType medicalType, OrganizationLevel level) {
        Organization organization = new Organization();
        organization.setId(1L);
        organization.setUuid(UUID.randomUUID());
        organization.setName("Test org");
        organization.setMedicalType(medicalType);
        organization.setLevelType(level);
        return organization;
    }

    private ResolvedOrganizationScope scope(OrganizationScopeMode mode, String regionCode, String districtCode) {
        return new ResolvedOrganizationScope(
                mode, 1L, UUID.randomUUID(), MedicalType.SANEPID_SERVICE, OrganizationLevel.REPUBLICAN,
                regionCode, districtCode
        );
    }

    @Test
    void loadRoot_allScope_returnsRepublicRootWithTotalAcrossAllOrganizations() {
        Organization organization = organization(MedicalType.SANEPID_SERVICE, OrganizationLevel.REPUBLICAN);
        when(organizationScopeResolver.resolve(organization)).thenReturn(scope(OrganizationScopeMode.ALL, null, null));
        when(organizationRepository.findActiveIdAndRegionCode()).thenReturn(List.of(
                new OrganizationGeoProjection(10L, "REG1"),
                new OrganizationGeoProjection(20L, "REG2")
        ));
        when(messageResolver.resolve("report.scope.republic")).thenReturn("Republic of Uzbekistan");

        ReportHierarchyNode<Long> root = reportHierarchyService.loadRoot(organization, LONG_COUNT_SOURCE, range, null);

        assertThat(root.code()).isEqualTo("UZ");
        assertThat(root.name()).isEqualTo("Republic of Uzbekistan");
        assertThat(root.hasChildren()).isTrue();
        assertThat(root.counts()).isEqualTo(20L);
    }

    @Test
    void loadChildren_allScopeNoParams_returnsOneNodePerRegionWithAggregatedCounts() {
        Organization organization = organization(MedicalType.SANEPID_SERVICE, OrganizationLevel.REPUBLICAN);
        when(organizationScopeResolver.resolve(organization)).thenReturn(scope(OrganizationScopeMode.ALL, null, null));

        Region region1 = new Region();
        region1.setCode("REG1");
        Region region2 = new Region();
        region2.setCode("REG2");
        when(regionRepository.findAllByDeletedFalseOrderByNameUzAsc()).thenReturn(List.of(region1, region2));
        when(organizationRepository.findActiveIdAndRegionCode()).thenReturn(List.of(
                new OrganizationGeoProjection(10L, "REG1"),
                new OrganizationGeoProjection(20L, "REG2")
        ));
        when(referenceLookupService.getRegionName("REG1")).thenReturn("Region One");
        when(referenceLookupService.getRegionName("REG2")).thenReturn("Region Two");

        List<ReportHierarchyNode<Long>> children = reportHierarchyService.loadChildren(
                organization, null, null, LONG_COUNT_SOURCE, range, null
        );

        assertThat(children).hasSize(2);
        assertThat(children.get(0).code()).isEqualTo("REG1");
        assertThat(children.get(0).name()).isEqualTo("Region One");
        assertThat(children.get(0).hasChildren()).isTrue();
        assertThat(children.get(0).counts()).isEqualTo(5L);
        assertThat(children.get(1).counts()).isEqualTo(7L);
    }

    @Test
    void loadChildren_regionScope_rejectsOutOfScopeRegionCode() {
        Organization organization = organization(MedicalType.SANEPID_SERVICE, OrganizationLevel.REGIONAL);
        when(organizationScopeResolver.resolve(organization))
                .thenReturn(scope(OrganizationScopeMode.REGION, "REG1", null));

        assertThatThrownBy(() -> reportHierarchyService.loadChildren(
                organization, "REG2", null, LONG_COUNT_SOURCE, range, null
        )).isInstanceOf(ScopeViolationException.class);
    }

    @Test
    void loadChildren_organizationScope_returnsEmptyWithoutQueryingGeography() {
        Organization organization = organization(MedicalType.HOSPITAL, OrganizationLevel.NOT_DEFINED);
        when(organizationScopeResolver.resolve(organization))
                .thenReturn(scope(OrganizationScopeMode.ORGANIZATION, null, null));

        List<ReportHierarchyNode<Long>> children = reportHierarchyService.loadChildren(
                organization, null, null, LONG_COUNT_SOURCE, range, null
        );

        assertThat(children).isEmpty();
    }

    @Test
    void loadRootBreakdown_allScope_appendsTrailingTotalRowAfterEachRegion() {
        Organization organization = organization(MedicalType.SANEPID_SERVICE, OrganizationLevel.REPUBLICAN);
        when(organizationScopeResolver.resolve(organization)).thenReturn(scope(OrganizationScopeMode.ALL, null, null));

        Region region1 = new Region();
        region1.setCode("REG1");
        Region region2 = new Region();
        region2.setCode("REG2");
        when(regionRepository.findAllByDeletedFalseOrderByNameUzAsc()).thenReturn(List.of(region1, region2));
        when(organizationRepository.findActiveIdAndRegionCode()).thenReturn(List.of(
                new OrganizationGeoProjection(10L, "REG1"),
                new OrganizationGeoProjection(20L, "REG2")
        ));
        when(referenceLookupService.getRegionName("REG1")).thenReturn("Region One");
        when(referenceLookupService.getRegionName("REG2")).thenReturn("Region Two");
        when(messageResolver.resolve("report.scope.total")).thenReturn("Jami");

        List<ReportHierarchyNode<Long>> root = reportHierarchyService.loadRootBreakdown(
                organization, LONG_COUNT_SOURCE, range, null
        );

        assertThat(root).hasSize(3);
        assertThat(root.get(0).code()).isEqualTo("REG1");
        assertThat(root.get(1).code()).isEqualTo("REG2");
        assertThat(root.get(2).code()).isEqualTo("TOTAL");
        assertThat(root.get(2).name()).isEqualTo("Jami");
        assertThat(root.get(2).hasChildren()).isFalse();
        assertThat(root.get(2).counts()).isEqualTo(20L);
    }

    @Test
    void loadRootBreakdown_organizationScope_fallsBackToSingleOwnOrganizationRow() {
        Organization organization = organization(MedicalType.HOSPITAL, OrganizationLevel.NOT_DEFINED);
        when(organizationScopeResolver.resolve(organization))
                .thenReturn(scope(OrganizationScopeMode.ORGANIZATION, null, null));
        when(organizationNameResolver.resolve(organization)).thenReturn("My Hospital");

        List<ReportHierarchyNode<Long>> root = reportHierarchyService.loadRootBreakdown(
                organization, LONG_COUNT_SOURCE, range, null
        );

        assertThat(root).hasSize(1);
        assertThat(root.get(0).code()).isEqualTo("1");
        assertThat(root.get(0).name()).isEqualTo("My Hospital");
        assertThat(root.get(0).hasChildren()).isFalse();
        assertThat(root.get(0).counts()).isEqualTo(10L);
    }
}

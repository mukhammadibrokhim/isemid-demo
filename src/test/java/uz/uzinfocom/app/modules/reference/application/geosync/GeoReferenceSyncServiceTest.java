package uz.uzinfocom.app.modules.reference.application.geosync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import uz.uzinfocom.app.modules.reference.application.common.event.DistrictChangedEvent;
import uz.uzinfocom.app.modules.reference.application.common.event.NeighborhoodChangedEvent;
import uz.uzinfocom.app.modules.reference.application.geosync.dto.DistrictSyncResult;
import uz.uzinfocom.app.modules.reference.application.geosync.dto.EgovNeighborhoodRow;
import uz.uzinfocom.app.modules.reference.application.geosync.dto.EgovSoatoRow;
import uz.uzinfocom.app.modules.reference.application.geosync.dto.NeighborhoodSyncResult;
import uz.uzinfocom.app.modules.reference.domain.District;
import uz.uzinfocom.app.modules.reference.domain.Neighborhood;
import uz.uzinfocom.app.modules.reference.domain.Region;
import uz.uzinfocom.app.modules.reference.domain.enums.AdministrativeType;
import uz.uzinfocom.app.modules.reference.repository.DistrictRepository;
import uz.uzinfocom.app.modules.reference.repository.NeighborhoodRepository;
import uz.uzinfocom.app.modules.reference.repository.RegionRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GeoReferenceSyncServiceTest {

    private final EgovClassifierClient client = mock(EgovClassifierClient.class);
    private final RegionRepository regionRepository = mock(RegionRepository.class);
    private final DistrictRepository districtRepository = mock(DistrictRepository.class);
    private final NeighborhoodRepository neighborhoodRepository = mock(NeighborhoodRepository.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
    private final TransactionStatus transactionStatus = mock(TransactionStatus.class);

    private GeoReferenceSyncService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        when(transactionTemplate.execute(any())).thenAnswer(invocation ->
                ((TransactionCallback<Object>) invocation.getArgument(0)).doInTransaction(transactionStatus));
        service = new GeoReferenceSyncService(
                client, regionRepository, districtRepository, neighborhoodRepository, eventPublisher, transactionTemplate);

        when(regionRepository.findAllByDeletedFalseOrderByNameUzAsc()).thenReturn(List.of(region("UZ-AN", 1703, 17)));
        when(districtRepository.findAll()).thenReturn(List.of(district("AN-202", 1703202, false)));
        when(client.fetchSoato()).thenReturn(List.of(new EgovSoatoRow("1703202", "Алтынкульский район")));
        when(neighborhoodRepository.findAll()).thenReturn(List.of());
        when(client.fetchNeighborhoods()).thenReturn(List.of());
    }

    // ------------------------------------------------------------ neighborhoods

    @Test
    void createsMissingNeighborhoodContinuingTheDistrictSequence() {
        Neighborhood a = neighborhood(1L, "AN-202001", "AN-202", 1703202001, "100000001", false);
        // A code that merely starts with the parent code but belongs to another district: not a sequence.
        Neighborhood foreign = neighborhood(2L, "AN-202365001", "AN-202365", 1703202365, "100000002", false);
        when(neighborhoodRepository.findAll()).thenReturn(List.of(a, foreign));
        when(client.fetchNeighborhoods()).thenReturn(List.of(
                row("100000001", "1703202", "1703202001", "Bog'", "Боғ", "Бог", "23010008"),
                row("100000009", "1703202", "1703202009", "Yangi", "Янги", "Новый", "23010099")));

        NeighborhoodSyncResult result = service.syncNeighborhoods(false);

        assertThat(result.created()).isEqualTo(1);
        assertThat(result.unchanged()).isZero();
        Neighborhood created = savedNeighborhoods().stream().filter(n -> n.getId() == null).findFirst().orElseThrow();
        assertThat(created.getCode()).isEqualTo("AN-202002");
        assertThat(created.getParentCode()).isEqualTo("AN-202");
        assertThat(created.getSoatoId()).isEqualTo(1703202009);
        assertThat(created.getParentSoatoId()).isEqualTo(1703202);
        assertThat(created.getTin()).isEqualTo("100000009");
        assertThat(created.getUzcadRegistryCode()).isEqualTo("23010099");
        assertThat(created.getNameUz()).isEqualTo("Yangi");
        assertThat(created.isDeleted()).isFalse();
        verify(eventPublisher).publishEvent(new NeighborhoodChangedEvent());
        verify(districtRepository, never()).saveAll(anyIterable());
    }

    @Test
    void reactivatesSoftDeletedNeighborhoodAndMovesItToTheNewDistrict() {
        when(districtRepository.findAll()).thenReturn(List.of(
                district("AN-202", 1703202, false), district("AN-203", 1703203, false)));
        Neighborhood deleted = neighborhood(7L, "AN-202005", "AN-202", 1703202005, "100000005", true);
        deleted.setNameUz("Eski nom");
        when(neighborhoodRepository.findAll()).thenReturn(List.of(deleted));
        when(client.fetchNeighborhoods()).thenReturn(List.of(
                row("100000005", "1703203", "1703202005", "Yangi nom", "Янги ном", "Новое", "23010005")));

        NeighborhoodSyncResult result = service.syncNeighborhoods(false);

        assertThat(result.reactivated()).isEqualTo(1);
        assertThat(result.moved()).isEqualTo(1);
        assertThat(result.fieldsUpdated()).isEqualTo(1);
        assertThat(result.created()).isZero();
        assertThat(deleted.isDeleted()).isFalse();
        assertThat(deleted.getParentCode()).isEqualTo("AN-203");
        assertThat(deleted.getParentSoatoId()).isEqualTo(1703203);
        assertThat(deleted.getNameUz()).isEqualTo("Yangi nom");
        assertThat(deleted.getUzcadRegistryCode()).isEqualTo("23010005");
        assertThat(savedNeighborhoods()).containsExactly(deleted);
    }

    @Test
    void matchesBySoatoIdWhenTheTinChanged() {
        Neighborhood existing = neighborhood(3L, "AN-202003", "AN-202", 1703202003, "100000003", false);
        fillNames(existing);
        when(neighborhoodRepository.findAll()).thenReturn(List.of(existing));
        when(client.fetchNeighborhoods()).thenReturn(List.of(
                row("999999999", "1703202", "1703202003", "Nom", "Ном", "Имя", "23010003")));

        NeighborhoodSyncResult result = service.syncNeighborhoods(false);

        assertThat(result.created()).isZero();
        assertThat(result.fieldsUpdated()).isEqualTo(1);
        assertThat(existing.getTin()).isEqualTo("999999999");
        assertThat(existing.getCode()).isEqualTo("AN-202003");
    }

    @Test
    void leavesIdenticalNeighborhoodUntouched() {
        Neighborhood existing = neighborhood(3L, "AN-202003", "AN-202", 1703202003, "100000003", false);
        fillNames(existing);
        when(neighborhoodRepository.findAll()).thenReturn(List.of(existing));
        when(client.fetchNeighborhoods()).thenReturn(List.of(
                row("100000003", "1703202", "1703202003", "Nom", "Ном", "Имя", "23010003")));

        NeighborhoodSyncResult result = service.syncNeighborhoods(false);

        assertThat(result.unchanged()).isEqualTo(1);
        assertThat(savedNeighborhoods()).isEmpty();
        verify(eventPublisher, never()).publishEvent(new NeighborhoodChangedEvent());
    }

    @Test
    void skipsInvalidRowsAndReportsNeighborhoodsOfUnknownDistricts() {
        when(client.fetchNeighborhoods()).thenReturn(List.of(
                row("100000001", "1714365", "1714401081", "A", "А", "А", "1"),
                row("100000002", "1714365", "1714401075", "B", "Б", "Б", "2"),
                row("312579214", "0", "0", "Yangi Andijon", "Янги Андижон", "Новый Андижан", "23010001"),
                row("", "1703202", "1703202010", "X", "Х", "Х", "3")));

        NeighborhoodSyncResult result = service.syncNeighborhoods(false);

        assertThat(result.skippedUnknownDistrict()).isEqualTo(2);
        assertThat(result.skippedInvalid()).isEqualTo(2);
        assertThat(result.unresolvedDistricts())
                .containsExactly(new NeighborhoodSyncResult.UnresolvedDistrict(1714365, 2));
        assertThat(result.invalidTins()).containsExactly("312579214");
        assertThat(savedNeighborhoods()).isEmpty();
    }

    @Test
    void attachesNeighborhoodsToATenDigitDistrictThroughEgovsShortCode() {
        // egov says 1703365, we store the district as 1703401365 (region + city 401 + 365).
        District davlatobod = district("AN-401365", 1703401365, false);
        when(districtRepository.findAll()).thenReturn(List.of(district("AN-202", 1703202, false), davlatobod));
        when(client.fetchNeighborhoods()).thenReturn(List.of(
                row("100000009", "1703365", "1703401081", "Elobod", "Элобод", "Элобод", "23010099")));

        NeighborhoodSyncResult result = service.syncNeighborhoods(false);

        assertThat(result.skippedUnknownDistrict()).isZero();
        assertThat(result.created()).isEqualTo(1);
        assertThat(result.aliasedDistricts())
                .containsExactly(new NeighborhoodSyncResult.AliasedDistrict(1703365, "AN-401365", 1703401365, 1));
        Neighborhood created = savedNeighborhoods().getFirst();
        assertThat(created.getParentCode()).isEqualTo("AN-401365");
        // parent_soato_id holds OUR district's SOATO id, not egov's short alias.
        assertThat(created.getParentSoatoId()).isEqualTo(1703401365);
        assertThat(created.getCode()).isEqualTo("AN-401365001");
    }

    @Test
    void doesNotAliasWhenARealDistrictOwnsTheShortKey() {
        when(districtRepository.findAll()).thenReturn(List.of(
                district("AN-365", 1703365, false), district("AN-401365", 1703401365, false)));
        when(client.fetchNeighborhoods()).thenReturn(List.of(
                row("100000009", "1703365", "1703365001", "Yangi", "Янги", "Новый", "23010099")));

        NeighborhoodSyncResult result = service.syncNeighborhoods(false);

        assertThat(result.aliasedDistricts()).isEmpty();
        assertThat(savedNeighborhoods().getFirst().getParentCode()).isEqualTo("AN-365");
    }

    @Test
    void doesNotAttachNeighborhoodsToASoftDeletedDistrict() {
        when(districtRepository.findAll()).thenReturn(List.of(district("AN-202", 1703202, true)));
        when(client.fetchNeighborhoods()).thenReturn(List.of(
                row("100000009", "1703202", "1703202009", "Yangi", "Янги", "Новый", "23010099")));

        NeighborhoodSyncResult result = service.syncNeighborhoods(false);

        assertThat(result.created()).isZero();
        assertThat(result.skippedUnknownDistrict()).isEqualTo(1);
    }

    @Test
    void neighborhoodDryRunReportsTheChangesButSavesNothingAndRollsBack() {
        when(client.fetchNeighborhoods()).thenReturn(List.of(
                row("100000009", "1703202", "1703202009", "Yangi", "Янги", "Новый", "23010099")));

        NeighborhoodSyncResult result = service.syncNeighborhoods(true);

        assertThat(result.dryRun()).isTrue();
        assertThat(result.created()).isEqualTo(1);
        verify(transactionStatus).setRollbackOnly();
        verify(neighborhoodRepository, never()).saveAll(anyIterable());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    // ---------------------------------------------------------------- districts

    @Test
    void createsMissingDistrictFromTheSoatoList() {
        when(client.fetchSoato()).thenReturn(List.of(
                new EgovSoatoRow("1703", "Андижанская область"),
                new EgovSoatoRow("1703202", "Алтынкульский район"),
                new EgovSoatoRow("1703401", "город Андижан")));

        DistrictSyncResult result = service.syncDistricts(false);

        assertThat(result.received()).isEqualTo(2);
        assertThat(result.created()).isEqualTo(1);
        assertThat(result.unchanged()).isEqualTo(1);
        assertThat(result.createdDistricts())
                .containsExactly(new DistrictSyncResult.CreatedDistrict("AN-401", 1703401, "город Андижан"));

        ArgumentCaptor<List<District>> districts = districtCaptor();
        verify(districtRepository).saveAll(districts.capture());
        District created = districts.getValue().getFirst();
        assertThat(created.getCode()).isEqualTo("AN-401");
        assertThat(created.getParentCode()).isEqualTo("UZ-AN");
        assertThat(created.getParentSoatoId()).isEqualTo(1703);
        assertThat(created.getLegacySoatoId()).isEqualTo(1703401);
        assertThat(created.getParentLegacySoatoId()).isEqualTo(17);
        assertThat(created.getAdministrativeType()).isEqualTo(AdministrativeType.CITY);
        assertThat(created.getNameUz()).isEqualTo("город Андижан");
        verify(eventPublisher).publishEvent(new DistrictChangedEvent());
        verify(neighborhoodRepository, never()).saveAll(anyIterable());
    }

    @Test
    void reactivatesSoftDeletedDistrict() {
        District deleted = district("AN-202", 1703202, true);
        when(districtRepository.findAll()).thenReturn(List.of(deleted));

        DistrictSyncResult result = service.syncDistricts(false);

        assertThat(result.reactivated()).isEqualTo(1);
        assertThat(result.reactivatedDistricts()).containsExactly("AN-202");
        assertThat(deleted.isDeleted()).isFalse();
    }

    @Test
    void skipsADistrictWhoseRegionOrCodeCannotBeResolved() {
        when(client.fetchSoato()).thenReturn(List.of(
                new EgovSoatoRow("1799001", "Неизвестный район"),
                new EgovSoatoRow("1703202", "Алтынкульский район")));
        when(districtRepository.findAll()).thenReturn(List.of(district("AN-202", 0, false)));

        DistrictSyncResult result = service.syncDistricts(false);

        // 1703202 is unknown by soato but its code AN-202 is taken -> conflict, not a duplicate row.
        assertThat(result.skippedDistricts()).containsExactlyInAnyOrder(
                new DistrictSyncResult.SkippedDistrict("1799001", "region_not_found"),
                new DistrictSyncResult.SkippedDistrict("1703202", "code_conflict"));
        assertThat(result.created()).isZero();
    }

    @Test
    void districtDryRunSavesNothingAndRollsBack() {
        when(client.fetchSoato()).thenReturn(List.of(new EgovSoatoRow("1703401", "город Андижан")));

        DistrictSyncResult result = service.syncDistricts(true);

        assertThat(result.dryRun()).isTrue();
        assertThat(result.created()).isEqualTo(1);
        verify(transactionStatus).setRollbackOnly();
        verify(districtRepository, never()).saveAll(anyIterable());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    // ------------------------------------------------------------------ helpers

    private List<Neighborhood> savedNeighborhoods() {
        ArgumentCaptor<List<Neighborhood>> captor = neighborhoodCaptor();
        verify(neighborhoodRepository).saveAll(captor.capture());
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private static ArgumentCaptor<List<Neighborhood>> neighborhoodCaptor() {
        return ArgumentCaptor.forClass(List.class);
    }

    @SuppressWarnings("unchecked")
    private static ArgumentCaptor<List<District>> districtCaptor() {
        return ArgumentCaptor.forClass(List.class);
    }

    private static void fillNames(Neighborhood neighborhood) {
        neighborhood.setNameUz("Nom");
        neighborhood.setNameUzCyril("Ном");
        neighborhood.setNameRu("Имя");
        neighborhood.setUzcadRegistryCode("23010003");
    }

    private static Region region(String code, int soato, int legacy) {
        return Region.builder().code(code).parentCode("UZ").soatoId(soato).legacySoatoId(legacy)
                .nameUz("Viloyat").deleted(false).build();
    }

    private static District district(String code, int soato, boolean deleted) {
        return District.builder().code(code).parentCode("UZ-AN").soatoId(soato).parentSoatoId(1703)
                .legacySoatoId(soato).parentLegacySoatoId(17).administrativeType(AdministrativeType.DISTRICT)
                .deleted(deleted).build();
    }

    private static Neighborhood neighborhood(long id, String code, String parentCode, int soato, String tin, boolean deleted) {
        Neighborhood neighborhood = Neighborhood.builder().code(code).parentCode(parentCode).soatoId(soato)
                .parentSoatoId(parentCode.equals("AN-203") ? 1703203 : 1703202)
                .tin(tin).deleted(deleted).build();
        neighborhood.setId(id);
        return neighborhood;
    }

    private static EgovNeighborhoodRow row(
            String tin, String districtSoato, String uzcad, String nameLatin, String nameCyril, String nameRu, String code
    ) {
        return new EgovNeighborhoodRow(tin, "1703", districtSoato, nameLatin, nameCyril, nameRu, nameLatin, uzcad, code);
    }
}

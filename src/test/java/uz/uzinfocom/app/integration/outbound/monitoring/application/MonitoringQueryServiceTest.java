package uz.uzinfocom.app.integration.outbound.monitoring.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uz.uzinfocom.app.integration.outbound.monitoring.infrastructure.MonitoringQueryRepository;
import uz.uzinfocom.app.integration.outbound.monitoring.web.dto.MonitoringResponse;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MonitoringQueryServiceTest {

    private static final LocalDate FROM = LocalDate.of(2026, 1, 1);
    private static final LocalDate TO = LocalDate.of(2026, 1, 31);

    private MonitoringQueryRepository repository;
    private MonitoringQueryService service;

    @BeforeEach
    void setUp() {
        repository = mock(MonitoringQueryRepository.class);
        service = new MonitoringQueryService(
                repository, new MonitoringResponseMapper(), new MonitoringDynamicsResponseMapper());
    }

    @Test
    void rejectsMissingDates() {
        assertThatThrownBy(() -> service.getMonitoring(null, TO, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getMonitoring(FROM, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsReversedRange() {
        assertThatThrownBy(() -> service.getMonitoring(TO, FROM, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsRangeOverTheCap() {
        LocalDate tooFar = FROM.plusDays(MonitoringQueryService.MAX_RANGE_DAYS);

        assertThatThrownBy(() -> service.getMonitoring(FROM, tooFar, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void acceptsRangeExactlyAtTheCap() {
        LocalDate lastDay = FROM.plusDays(MonitoringQueryService.MAX_RANGE_DAYS - 1);
        when(repository.findLocationStatistics(any())).thenReturn(List.of());
        when(repository.findDiseaseStatistics(any())).thenReturn(List.of());

        assertThat(service.getMonitoring(FROM, lastDay, null, null).data()).isEmpty();
    }

    @Test
    void normalizesRegionAndDiagnosisFilters() {
        when(repository.findLocationStatistics(any())).thenReturn(List.of());
        when(repository.findDiseaseStatistics(any())).thenReturn(List.of());

        service.getMonitoring(FROM, TO, "  uz-an  ", " ((a00-a09)) ");

        ArgumentCaptor<MonitoringFilter> captor = ArgumentCaptor.forClass(MonitoringFilter.class);
        verify(repository).findLocationStatistics(captor.capture());
        assertThat(captor.getValue().regionCode()).isEqualTo("UZ-AN");
        assertThat(captor.getValue().diagnosisCode()).isEqualTo("A00-A09");
    }

    @Test
    void blankFiltersMeanNoFilter() {
        when(repository.findLocationStatistics(any())).thenReturn(List.of());
        when(repository.findDiseaseStatistics(any())).thenReturn(List.of());

        service.getMonitoring(FROM, TO, "  ", "()");

        ArgumentCaptor<MonitoringFilter> captor = ArgumentCaptor.forClass(MonitoringFilter.class);
        verify(repository).findLocationStatistics(captor.capture());
        assertThat(captor.getValue().hasRegion()).isFalse();
        assertThat(captor.getValue().hasDiagnosis()).isFalse();
    }

    @Test
    void mergesDiseaseRowsIntoTheirLocationAndKeepsEmptyLocations() {
        when(repository.findLocationStatistics(any())).thenReturn(List.of(
                new MonitoringLocationRow("1726", 3, 1, 2, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1),
                new MonitoringLocationRow("1727", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        ));
        when(repository.findDiseaseStatistics(any())).thenReturn(List.of(
                new MonitoringDiseaseRow("1726", "A00-A09", 2),
                new MonitoringDiseaseRow("1726", "(B15-B19)", 1),
                new MonitoringDiseaseRow("1726", "UNCLASSIFIED", 0)
        ));

        MonitoringResponse response = service.getMonitoring(FROM, TO, null, null);

        assertThat(response.toDate()).isEqualTo(TO);
        assertThat(response.data()).hasSize(2);

        MonitoringResponse.Item first = response.data().getFirst();
        assertThat(first.regionCode()).isEqualTo("1726");
        assertThat(first.ageGroups().age_18_25()).isEqualTo(1);
        assertThat(first.ageGroups().age_unknown()).isEqualTo(1);
        assertThat(first.diseaseType())
                .containsEntry("(A00-A09)", 2)
                .containsEntry("(B15-B19)", 1)
                .containsEntry("UNCLASSIFIED", 0);

        assertThat(response.data().get(1).diseaseType()).isEmpty();
    }

    @Test
    void locationRowTotalsMustPartitionTotalCases() {
        assertThat(new MonitoringLocationRow("1", 3, 1, 2, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1)
                .hasConsistentTotals()).isTrue();
        assertThat(new MonitoringLocationRow("1", 3, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1)
                .hasConsistentTotals()).isFalse();
    }
}

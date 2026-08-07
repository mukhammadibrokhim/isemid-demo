package uz.uzinfocom.app.platform.devmonitoring.application;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uz.uzinfocom.app.platform.devmonitoring.application.stream.DevSseBroadcaster;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevErrorLog;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevErrorStatus;
import uz.uzinfocom.app.platform.devmonitoring.repository.DevErrorLogRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DevErrorLogWriterTest {

    private final DevErrorLogRepository devErrorLogRepository = mock(DevErrorLogRepository.class);
    private final DevSseBroadcaster devSseBroadcaster = mock(DevSseBroadcaster.class);
    private final DevErrorLogWriter writer = new DevErrorLogWriter(devErrorLogRepository, devSseBroadcaster);

    @Test
    void persistsAllSuppliedFieldsWithOpenStatusByDefault() {
        when(devErrorLogRepository.save(any(DevErrorLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        writer.record("trace-1", "NOT_FOUND", 404, "uz.uzinfocom.app.shared.exception.NotFoundException",
                "/v1/users/999", "GET", "user-1", "User was not found: 999");

        ArgumentCaptor<DevErrorLog> captor = ArgumentCaptor.forClass(DevErrorLog.class);
        verify(devErrorLogRepository).save(captor.capture());

        DevErrorLog saved = captor.getValue();
        assertThat(saved.getTraceId()).isEqualTo("trace-1");
        assertThat(saved.getErrorCode()).isEqualTo("NOT_FOUND");
        assertThat(saved.getHttpStatus()).isEqualTo(404);
        assertThat(saved.getExceptionType()).isEqualTo("uz.uzinfocom.app.shared.exception.NotFoundException");
        assertThat(saved.getPath()).isEqualTo("/v1/users/999");
        assertThat(saved.getMethod()).isEqualTo("GET");
        assertThat(saved.getPrincipal()).isEqualTo("user-1");
        assertThat(saved.getMessage()).isEqualTo("User was not found: 999");
        assertThat(saved.getOccurredAt()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(DevErrorStatus.OPEN);
    }

    @Test
    void swallowsAPersistenceFailureRatherThanPropagatingIt() {
        when(devErrorLogRepository.save(any(DevErrorLog.class))).thenThrow(new RuntimeException("db down"));

        writer.record("trace-2", "INTERNAL_ERROR", 500, "java.lang.RuntimeException",
                "/v1/anything", "POST", null, "boom");

        // No exception propagates - this is a best-effort audit write, never allowed
        // to disrupt the request/response cycle it's observing.
    }
}
